package com.example.iemride;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.iemride.databinding.FragmentOfferRideBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class OfferRideFragment extends Fragment {

    private FragmentOfferRideBinding binding;
    private DatabaseReference ridesRef;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private boolean isPublishingRide = false;
    private static final String TAG = "OfferRideFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOfferRideBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firebase services
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        ridesRef = database.getReference("rides");
        db = FirebaseFirestore.getInstance(); // Still use Firestore for user data
        mAuth = FirebaseAuth.getInstance();

        binding.publishRideButton.setOnClickListener(v -> {
            if (!isPublishingRide) {
                publishRide();
            }
        });
    }

    private void publishRide() {
        String departure = binding.departureEditText.getText().toString().trim();
        String destination = binding.destinationEditText.getText().toString().trim();
        String time = binding.timeEditText.getText().toString().trim();
        String seatsStr = binding.seatsEditText.getText().toString().trim();
        String priceStr = binding.priceEditText.getText().toString().trim();

        if (TextUtils.isEmpty(departure) || TextUtils.isEmpty(destination) ||
                TextUtils.isEmpty(time) || TextUtils.isEmpty(seatsStr) || TextUtils.isEmpty(priceStr)) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if already publishing
        if (isPublishingRide) {
            Toast.makeText(getContext(), "Please wait, publishing ride...", Toast.LENGTH_SHORT).show();
            return;
        }

        // Set publishing flag and disable button
        isPublishingRide = true;
        binding.publishRideButton.setEnabled(false);
        binding.publishRideButton.setText("Publishing...");

        try {
            int seats = Integer.parseInt(seatsStr);
            double price = Double.parseDouble(priceStr);

            if (seats <= 0) {
                Toast.makeText(getContext(), "Please enter valid number of seats", Toast.LENGTH_SHORT).show();
                resetPublishButton();
                return;
            }

            if (price <= 0) {
                Toast.makeText(getContext(), "Please enter valid price", Toast.LENGTH_SHORT).show();
                resetPublishButton();
                return;
            }

            String userId = mAuth.getCurrentUser().getUid();

            int selectedVehicleId = binding.vehicleTypeRadioGroup.getCheckedRadioButtonId();
            if (selectedVehicleId == -1) {
                Toast.makeText(getContext(), "Please select a vehicle type", Toast.LENGTH_SHORT).show();
                resetPublishButton();
                return;
            }

            RadioButton selectedRadioButton = getView().findViewById(selectedVehicleId);
            String vehicleType = selectedRadioButton.getText().toString();

            // Get user profile data from Firestore to populate driver info
            getUserProfileAndPublishRide(departure, destination, time, vehicleType, seats, price, userId);

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please enter valid numbers for seats and price", Toast.LENGTH_SHORT).show();
            resetPublishButton();
        }
    }

    private void getUserProfileAndPublishRide(String departure, String destination, String time,
                                              String vehicleType, int seats, double price, String userId) {

        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    String driverName = "User"; // Default name
                    String vehicleModel = "N/A"; // Default vehicle
                    double driverRating = 4.5; // Default rating

                    if (task.isSuccessful() && task.getResult() != null) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            User user = document.toObject(User.class);
                            if (user != null) {
                                driverName = user.getName() != null ? user.getName() : "User";
                                vehicleModel = user.getVehicleModel() != null ? user.getVehicleModel() : "N/A";
                                // You can add a rating field to User model later
                            }
                        }
                    }

                    // Create a new Ride object
                    Ride ride = new Ride(driverName, vehicleModel, driverRating, departure,
                            destination, time, vehicleType, seats, price, userId);

                    // Push to Realtime Database
                    DatabaseReference newRideRef = ridesRef.push();
                    newRideRef.setValue(ride.toMap())
                            .addOnSuccessListener(aVoid -> {
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), "Ride published successfully!", Toast.LENGTH_SHORT).show();
                                    clearFields();
                                    resetPublishButton();
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Error publishing ride", e);
                                if (getContext() != null) {
                                    Toast.makeText(getContext(), "Error publishing ride: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    resetPublishButton();
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error getting user profile", e);
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error getting user profile", Toast.LENGTH_SHORT).show();
                        resetPublishButton();
                    }
                });
    }

    private void resetPublishButton() {
        isPublishingRide = false;
        if (binding != null) {
            binding.publishRideButton.setEnabled(true);
            binding.publishRideButton.setText("Publish Ride");
        }
    }

    private void clearFields() {
        if (binding != null) {
            binding.departureEditText.setText("");
            binding.destinationEditText.setText("");
            binding.timeEditText.setText("");
            binding.seatsEditText.setText("");
            binding.priceEditText.setText("");
            binding.carRadioButton.setChecked(true);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isPublishingRide = false;
        binding = null;
    }
}