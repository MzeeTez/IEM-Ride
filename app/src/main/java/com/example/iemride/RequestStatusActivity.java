package com.example.iemride;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.iemride.databinding.ActivityRequestStatusBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RequestStatusActivity extends AppCompatActivity {

    private ActivityRequestStatusBinding binding;
    private DatabaseReference requestRef;
    private ValueEventListener statusListener;
    private String requestId;
    private RideRequest currentRequest;
    private static final String TAG = "RequestStatusActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRequestStatusBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        requestId = getIntent().getStringExtra("requestId");
        currentRequest = (RideRequest) getIntent().getSerializableExtra("rideRequest");

        if (requestId == null || currentRequest == null) {
            Toast.makeText(this, "Request information not available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        requestRef = FirebaseDatabase.getInstance().getReference("rideRequests").child(requestId);

        setupUI();
        setupStatusListener();

        binding.backButton.setOnClickListener(v -> finish());
    }

    private void setupUI() {
        binding.pickupLocationTextView.setText(currentRequest.getPickupLocation());
        binding.driverNameTextView.setText("Waiting for " + currentRequest.getFromUserName() + "'s response...");
        updateStatusUI(currentRequest.getStatus());
    }

    private void setupStatusListener() {
        statusListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    RideRequest updatedRequest = snapshot.getValue(RideRequest.class);
                    if (updatedRequest != null) {
                        currentRequest = updatedRequest;
                        updateStatusUI(updatedRequest.getStatus());

                        if ("accepted".equals(updatedRequest.getStatus())) {
                            // Request accepted - unlock maps feature
                            Toast.makeText(RequestStatusActivity.this, "Request accepted! Opening maps...", Toast.LENGTH_SHORT).show();
                            openMapsActivity();
                        } else if ("rejected".equals(updatedRequest.getStatus())) {
                            // Request rejected
                            Toast.makeText(RequestStatusActivity.this, "Request was rejected by the driver", Toast.LENGTH_LONG).show();
                        }
                    }
                } else {
                    // Request was deleted
                    Toast.makeText(RequestStatusActivity.this, "Request not found", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Database error: " + error.getMessage());
                Toast.makeText(RequestStatusActivity.this, "Error tracking request status", Toast.LENGTH_SHORT).show();
            }
        };

        requestRef.addValueEventListener(statusListener);
    }

    private void updateStatusUI(String status) {
        switch (status) {
            case "pending":
                binding.statusTextView.setText("Pending");
                binding.statusTextView.setTextColor(getColor(android.R.color.holo_orange_light));
                binding.statusDescriptionTextView.setText("Waiting for driver to respond to your request...");
                binding.progressBar.setVisibility(android.view.View.VISIBLE);
                binding.actionButton.setVisibility(android.view.View.GONE);
                break;

            case "accepted":
                binding.statusTextView.setText("Accepted ✓");
                binding.statusTextView.setTextColor(getColor(android.R.color.holo_green_light));
                binding.statusDescriptionTextView.setText("Great! The driver has accepted your request. You can now access maps to track your ride.");
                binding.progressBar.setVisibility(android.view.View.GONE);
                binding.actionButton.setVisibility(android.view.View.VISIBLE);
                binding.actionButton.setText("Open Maps");
                binding.actionButton.setOnClickListener(v -> openMapsActivity());
                break;

            case "rejected":
                binding.statusTextView.setText("Rejected ✗");
                binding.statusTextView.setTextColor(getColor(android.R.color.holo_red_light));
                binding.statusDescriptionTextView.setText("Sorry, the driver has rejected your request. Try booking another ride.");
                binding.progressBar.setVisibility(android.view.View.GONE);
                binding.actionButton.setVisibility(android.view.View.VISIBLE);
                binding.actionButton.setText("Back to Dashboard");
                binding.actionButton.setOnClickListener(v -> {
                    Intent intent = new Intent(RequestStatusActivity.this, HomeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                });
                break;
        }
    }

    private void openMapsActivity() {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("requestId", requestId);
        intent.putExtra("rideRequest", currentRequest);
        intent.putExtra("isDriver", false); // Passenger perspective
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (requestRef != null && statusListener != null) {
            requestRef.removeEventListener(statusListener);
        }
    }
}