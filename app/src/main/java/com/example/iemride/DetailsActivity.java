package com.example.iemride;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.example.iemride.databinding.ActivityDetailsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Locale;

public class DetailsActivity extends AppCompatActivity {

    private ActivityDetailsBinding binding;
    private Ride currentRide;
    private DatabaseReference requestsRef;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private static final String TAG = "DetailsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        requestsRef = FirebaseDatabase.getInstance().getReference("rideRequests");
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        currentRide = (Ride) getIntent().getSerializableExtra("ride");

        if (currentRide != null) {
            binding.driverNameTextView.setText(currentRide.getDriverName());
            binding.vehicleModelTextView.setText(currentRide.getVehicleModel());
            binding.priceTextView.setText(String.format(Locale.getDefault(), "₹%.0f", currentRide.getPricePerSeat()));
            binding.seatsAvailableTextView.setText(String.format(Locale.getDefault(), "%d seats", currentRide.getSeatsAvailable()));
        }

        binding.requestButton.setOnClickListener(v -> showPickupLocationDialog());
    }

    private void showPickupLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Pickup Location");

        // Create an EditText for pickup location
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("e.g., IEM College Gate, New Town, etc.");
        builder.setView(input);

        builder.setPositiveButton("Send Request", (dialog, which) -> {
            String pickupLocation = input.getText().toString().trim();
            if (TextUtils.isEmpty(pickupLocation)) {
                Toast.makeText(this, "Please enter pickup location", Toast.LENGTH_SHORT).show();
                return;
            }
            sendRideRequest(pickupLocation);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void sendRideRequest(String pickupLocation) {
        if (currentRide == null) {
            Toast.makeText(this, "Ride information not available", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentUserId = mAuth.getCurrentUser().getUid();

        // Check if user is trying to request their own ride
        if (currentUserId.equals(currentRide.getUserId())) {
            Toast.makeText(this, "You cannot request your own ride", Toast.LENGTH_SHORT).show();
            return;
        }

        // Disable button to prevent multiple clicks
        binding.requestButton.setEnabled(false);
        binding.requestButton.setText("Sending...");

        // Get current user profile for request details
        db.collection("users").document(currentUserId).get()
                .addOnCompleteListener(task -> {
                    String userName;
                    String userPhone = "";

                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            User user = document.toObject(User.class);
                            if (user != null) {
                                userName = user.getName() != null ? user.getName() : "User";
                                userPhone = user.getPhoneNumber() != null ? user.getPhoneNumber() : "";
                            } else {
                                userName = "User";
                            }
                        } else {
                            userName = "User";
                        }
                    } else {
                        userName = "User";
                    }

                    // Create ride request
                    RideRequest rideRequest = new RideRequest(
                            currentUserId,
                            userName,
                            userPhone,
                            currentRide.getUserId(), // Driver ID
                            pickupLocation,
                            currentRide.getRideId()
                    );

                    // Save to Firebase
                    DatabaseReference newRequestRef = requestsRef.push();
                    String requestId = newRequestRef.getKey();
                    rideRequest.setRequestId(requestId);

                    newRequestRef.setValue(rideRequest.toMap())
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Ride request sent successfully!", Toast.LENGTH_SHORT).show();

                                // Send FCM notification to driver
                                NotificationHelper.sendRideRequestNotification(
                                        currentRide.getUserId(), // Driver ID
                                        userName,
                                        pickupLocation,
                                        requestId
                                );

                                // Navigate to request status activity
                                Intent intent = new Intent(DetailsActivity.this, RequestStatusActivity.class);
                                intent.putExtra("requestId", requestId);
                                intent.putExtra("rideRequest", rideRequest);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error sending ride request", e);
                                Toast.makeText(this, "Error sending request: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                                // Re-enable button on failure
                                binding.requestButton.setEnabled(true);
                                binding.requestButton.setText("Request Ride");
                            });
                });
    }
}