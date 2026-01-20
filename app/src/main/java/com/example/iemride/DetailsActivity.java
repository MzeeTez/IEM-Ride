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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Locale;

public class DetailsActivity extends AppCompatActivity {

    private ActivityDetailsBinding binding;
    private Ride currentRide;
    private DatabaseReference requestsRef;
    private DatabaseReference ridesRef;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private boolean isRequestInProgress = false;
    private static final String TAG = "DetailsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize Firebase
        requestsRef = FirebaseDatabase.getInstance().getReference("rideRequests");
        ridesRef = FirebaseDatabase.getInstance().getReference("rides");
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        currentRide = (Ride) getIntent().getSerializableExtra("ride");

        if (currentRide != null) {
            setupUI();
            checkSeatAvailability();
        }

        binding.requestButton.setOnClickListener(v -> {
            if (!isRequestInProgress) {
                showPickupLocationDialog();
            }
        });
    }

    private void setupUI() {
        binding.driverNameTextView.setText(currentRide.getDriverName());
        binding.vehicleModelTextView.setText(currentRide.getVehicleModel());
        binding.priceTextView.setText(String.format(Locale.getDefault(), "₹%.0f", currentRide.getPricePerSeat()));
        updateSeatsDisplay(currentRide.getSeatsAvailable());
    }

    private void updateSeatsDisplay(int availableSeats) {
        binding.seatsAvailableTextView.setText(String.format(Locale.getDefault(), "%d seats", availableSeats));

        // Disable book button if no seats available
        if (availableSeats <= 0) {
            binding.requestButton.setEnabled(false);
            binding.requestButton.setText("No Seats Available");
            binding.requestButton.setBackgroundColor(getColor(android.R.color.darker_gray));
        } else {
            binding.requestButton.setEnabled(true);
            binding.requestButton.setText("Request Ride");
            binding.requestButton.setBackgroundColor(getColor(R.color.primary_color));
        }
    }

    private void checkSeatAvailability() {
        if (currentRide.getRideId() != null) {
            ridesRef.child(currentRide.getRideId()).child("seatsAvailable")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Integer availableSeats = dataSnapshot.getValue(Integer.class);
                            if (availableSeats != null) {
                                currentRide.setSeatsAvailable(availableSeats);
                                updateSeatsDisplay(availableSeats);
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e(TAG, "Error checking seat availability: " + databaseError.getMessage());
                        }
                    });
        }
    }

    private void showPickupLocationDialog() {
        // Prevent multiple dialogs
        if (isRequestInProgress) {
            return;
        }

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

        // Prevent dialog from being dismissed by clicking outside
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void sendRideRequest(String pickupLocation) {
        if (currentRide == null) {
            Toast.makeText(this, "Ride information not available", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if request is already in progress
        if (isRequestInProgress) {
            return;
        }

        String currentUserId = mAuth.getCurrentUser().getUid();

        // Check if user is trying to request their own ride
        if (currentUserId.equals(currentRide.getUserId())) {
            Toast.makeText(this, "You cannot request your own ride", Toast.LENGTH_SHORT).show();
            return;
        }

        // Set request in progress flag
        isRequestInProgress = true;

        // Disable button to prevent multiple clicks
        binding.requestButton.setEnabled(false);
        binding.requestButton.setText("Sending...");

        // First check if seats are still available
        ridesRef.child(currentRide.getRideId()).child("seatsAvailable")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Integer availableSeats = dataSnapshot.getValue(Integer.class);

                        if (availableSeats == null || availableSeats <= 0) {
                            Toast.makeText(DetailsActivity.this, "No seats available anymore", Toast.LENGTH_SHORT).show();
                            resetRequestButton();
                            return;
                        }

                        // Proceed with request
                        proceedWithRideRequest(pickupLocation, currentUserId);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "Error checking seat availability", databaseError.toException());
                        resetRequestButton();
                    }
                });
    }

    private void proceedWithRideRequest(String pickupLocation, String currentUserId) {
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
                                resetRequestButton();
                            });
                });
    }

    private void resetRequestButton() {
        isRequestInProgress = false;
        binding.requestButton.setEnabled(true);
        binding.requestButton.setText("Request Ride");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRequestInProgress = false;
    }
}