package com.example.iemride;

import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.iemride.databinding.ActivityDetailsBinding;
import java.util.Locale;

public class DetailsActivity extends AppCompatActivity {

    private ActivityDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Ride ride = (Ride) getIntent().getSerializableExtra("ride");

        if (ride != null) {
            binding.driverNameTextView.setText(ride.getDriverName());
            binding.vehicleModelTextView.setText(ride.getVehicleModel());
            binding.priceTextView.setText(String.format(Locale.getDefault(), "₹%.0f", ride.getPricePerSeat()));
            binding.seatsAvailableTextView.setText(String.format(Locale.getDefault(), "%d seats", ride.getSeatsAvailable()));
        }

        binding.requestButton.setOnClickListener(v -> {
            // Logic for ride request will be added here
            Toast.makeText(this, "Ride requested!", Toast.LENGTH_SHORT).show();
        });
    }
}