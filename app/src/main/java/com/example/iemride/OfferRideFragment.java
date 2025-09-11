package com.example.iemride;

import android.os.Bundle;
import android.text.TextUtils;
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
import com.google.firebase.firestore.FirebaseFirestore;

public class OfferRideFragment extends Fragment {

    private FragmentOfferRideBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOfferRideBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        binding.publishRideButton.setOnClickListener(v -> publishRide());
    }

    private void publishRide() {
        String departure = binding.departureEditText.getText().toString().trim();
        String destination = binding.destinationEditText.getText().toString().trim();
        String time = binding.timeEditText.getText().toString().trim();
        String seatsStr = binding.seatsEditText.getText().toString().trim();
        String priceStr = binding.priceEditText.getText().toString().trim();

        if (TextUtils.isEmpty(departure) || TextUtils.isEmpty(destination) || TextUtils.isEmpty(time) || TextUtils.isEmpty(seatsStr) || TextUtils.isEmpty(priceStr)) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // === FIX IS HERE ===
        // Add placeholder data for the new fields.
        // You can get this data from the user's profile later.
        String driverName = "Aditya Singh"; // Placeholder
        String vehicleModel = "Maruti Swift"; // Placeholder
        double driverRating = 4.9; // Placeholder

        int seats = Integer.parseInt(seatsStr);
        double price = Double.parseDouble(priceStr);
        String userId = mAuth.getCurrentUser().getUid();

        int selectedVehicleId = binding.vehicleTypeRadioGroup.getCheckedRadioButtonId();
        RadioButton selectedRadioButton = getView().findViewById(selectedVehicleId);
        String vehicleType = selectedRadioButton.getText().toString();

        // Create a new Ride object with ALL the required fields
        Ride ride = new Ride(driverName, vehicleModel, driverRating, departure, destination, time, vehicleType, seats, price, userId);

        db.collection("rides")
                .add(ride)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "Ride published successfully!", Toast.LENGTH_SHORT).show();
                    clearFields();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Error publishing ride: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void clearFields() {
        binding.departureEditText.setText("");
        binding.destinationEditText.setText("");
        binding.timeEditText.setText("");
        binding.seatsEditText.setText("");
        binding.priceEditText.setText("");
        binding.carRadioButton.setChecked(true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}