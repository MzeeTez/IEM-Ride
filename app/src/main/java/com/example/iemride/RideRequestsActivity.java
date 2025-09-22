package com.example.iemride;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View; // Import View
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.iemride.databinding.ActivityRideRequestsBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RideRequestsActivity extends AppCompatActivity implements RideRequestsAdapter.OnRequestActionListener {

    private ActivityRideRequestsBinding binding;
    private DatabaseReference requestsRef;
    private RideRequestsAdapter adapter;
    private List<RideRequest> requestList;

    // --- FIX 1: Add a Query member variable to properly remove the listener later ---
    private Query requestsQuery;
    private ValueEventListener requestsListener;
    private String currentDriverId;
    private static final String TAG = "RideRequestsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRideRequestsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        currentDriverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        requestsRef = FirebaseDatabase.getInstance().getReference("rideRequests");

        setupRecyclerView();
        loadRideRequests();

        binding.backButton.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        requestList = new ArrayList<>();
        adapter = new RideRequestsAdapter(requestList, this);
        binding.requestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.requestsRecyclerView.setAdapter(adapter);
    }

    private void loadRideRequests() {
        // Query requests for the current driver
        requestsQuery = requestsRef.orderByChild("toDriverId").equalTo(currentDriverId);

        requestsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                requestList.clear();

                for (DataSnapshot requestSnapshot : dataSnapshot.getChildren()) {
                    try {
                        RideRequest request = requestSnapshot.getValue(RideRequest.class);
                        if (request != null) {
                            request.setRequestId(requestSnapshot.getKey());
                            requestList.add(request);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing request data: " + e.getMessage());
                    }
                }

                // Sort by timestamp (newest first)
                Collections.sort(requestList, new Comparator<RideRequest>() {
                    @Override
                    public int compare(RideRequest r1, RideRequest r2) {
                        Long timestamp1 = getTimestampAsLong(r1.getTimestamp());
                        Long timestamp2 = getTimestampAsLong(r2.getTimestamp());

                        if (timestamp1 == null && timestamp2 == null) return 0;
                        if (timestamp1 == null) return 1; // Treat nulls as older
                        if (timestamp2 == null) return -1;

                        return timestamp2.compareTo(timestamp1); // Descending order
                    }
                });

                adapter.notifyDataSetChanged();
                updateEmptyView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                Toast.makeText(RideRequestsActivity.this, "Error loading requests", Toast.LENGTH_SHORT).show();
            }
        };

        requestsQuery.addValueEventListener(requestsListener);
    }

    private Long getTimestampAsLong(Object timestamp) {
        if (timestamp instanceof Long) {
            return (Long) timestamp;
        } else if (timestamp instanceof Double) {
            return ((Double) timestamp).longValue();
        }
        return null;
    }

    private void updateEmptyView() {
        if (requestList.isEmpty()) {
            binding.requestsRecyclerView.setVisibility(View.GONE);
            binding.emptyView.setVisibility(View.VISIBLE);
        } else {
            binding.requestsRecyclerView.setVisibility(View.VISIBLE);
            binding.emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAcceptRequest(RideRequest request) {
        updateRequestStatus(request.getRequestId(), "accepted");
        Toast.makeText(this, "Request accepted! Opening maps...", Toast.LENGTH_SHORT).show();

        // Send FCM notification to passenger
        NotificationHelper.sendRequestStatusNotification(request.getFromUserId(), "accepted", request.getRequestId());

        // Open maps for driver
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("requestId", request.getRequestId());
        intent.putExtra("rideRequest", request);
        intent.putExtra("isDriver", true); // Driver perspective
        startActivity(intent);
    }

    @Override
    public void onRejectRequest(RideRequest request) {
        updateRequestStatus(request.getRequestId(), "rejected");
        Toast.makeText(this, "Request rejected", Toast.LENGTH_SHORT).show();

        // Send FCM notification to passenger
        NotificationHelper.sendRequestStatusNotification(request.getFromUserId(), "rejected", request.getRequestId());

        // --- FIX 2: Removed the duplicated, incomplete line of code that was here ---
    }

    private void updateRequestStatus(String requestId, String status) {
        DatabaseReference requestRef = requestsRef.child(requestId);
        requestRef.child("status").setValue(status)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating request status", e);
                    Toast.makeText(this, "Error updating request", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // --- FIX 3: Remove listener from the Query object, not the DatabaseReference, to prevent memory leaks ---
        if (requestsQuery != null && requestsListener != null) {
            requestsQuery.removeEventListener(requestsListener);
        }
    }
}