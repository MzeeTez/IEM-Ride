package com.example.iemride;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.iemride.databinding.FragmentDashboardBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private DatabaseReference ridesRef;
    private RideAdapter rideAdapter;
    private List<Ride> rideList;
    private ValueEventListener ridesListener;
    private static final String TAG = "DashboardFragment";

    // 18 hours in milliseconds
    private static final long EIGHTEEN_HOURS_MILLIS = 18 * 60 * 60 * 1000;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Realtime Database reference
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        ridesRef = database.getReference("rides");

        // Initialize the ride list and adapter
        rideList = new ArrayList<>();
        rideAdapter = new RideAdapter(rideList);

        // Set up the RecyclerView
        binding.ridesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.ridesRecyclerView.setAdapter(rideAdapter);

        // Set up notifications button click listener through the included app bar
        View appBarView = binding.appBar.getRoot();
        ImageButton notificationButton = appBarView.findViewById(R.id.notificationButton);
        notificationButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), RideRequestsActivity.class);
            startActivity(intent);
        });

        // Clean up old rides and load current rides
        cleanupOldRides();
        loadRides();
    }

    private void cleanupOldRides() {
        long currentTime = System.currentTimeMillis();
        long cutoffTime = currentTime - EIGHTEEN_HOURS_MILLIS;

        ridesRef.orderByChild("timestamp").endAt(cutoffTime).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot rideSnapshot : dataSnapshot.getChildren()) {
                    try {
                        Object timestampObj = rideSnapshot.child("timestamp").getValue();
                        Long timestamp = null;

                        if (timestampObj instanceof Long) {
                            timestamp = (Long) timestampObj;
                        } else if (timestampObj instanceof Double) {
                            timestamp = ((Double) timestampObj).longValue();
                        }

                        // Delete rides older than 18 hours
                        if (timestamp != null && timestamp < cutoffTime) {
                            Log.d(TAG, "Deleting old ride: " + rideSnapshot.getKey());
                            rideSnapshot.getRef().removeValue()
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Old ride deleted successfully"))
                                    .addOnFailureListener(e -> Log.e(TAG, "Error deleting old ride", e));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error processing ride for cleanup: " + e.getMessage());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Error during cleanup: " + databaseError.getMessage());
            }
        });
    }

    private void loadRides() {
        // Create a query to get rides ordered by timestamp (most recent first)
        Query ridesQuery = ridesRef.orderByChild("timestamp");

        ridesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                rideList.clear();
                long currentTime = System.currentTimeMillis();
                long cutoffTime = currentTime - EIGHTEEN_HOURS_MILLIS;

                for (DataSnapshot rideSnapshot : dataSnapshot.getChildren()) {
                    try {
                        Ride ride = rideSnapshot.getValue(Ride.class);
                        if (ride != null) {
                            // Check if ride is still valid (not older than 18 hours)
                            Long timestamp = getTimestampAsLong(ride.getTimestamp());

                            if (timestamp != null && timestamp >= cutoffTime) {
                                // Set the ride ID from the key
                                ride.setRideId(rideSnapshot.getKey());
                                rideList.add(ride);
                            } else if (timestamp != null && timestamp < cutoffTime) {
                                // Delete old ride
                                rideSnapshot.getRef().removeValue();
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing ride data: " + e.getMessage());
                    }
                }

                // Sort by timestamp in descending order (newest first)
                Collections.sort(rideList, new Comparator<Ride>() {
                    @Override
                    public int compare(Ride r1, Ride r2) {
                        // Handle timestamp comparison safely
                        Long timestamp1 = getTimestampAsLong(r1.getTimestamp());
                        Long timestamp2 = getTimestampAsLong(r2.getTimestamp());

                        if (timestamp1 == null && timestamp2 == null) return 0;
                        if (timestamp1 == null) return 1;
                        if (timestamp2 == null) return -1;

                        return timestamp2.compareTo(timestamp1); // Descending order
                    }
                });

                rideAdapter.notifyDataSetChanged();
                updateEmptyView();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Database error: " + databaseError.getMessage());
                updateEmptyView();
            }
        };

        ridesQuery.addValueEventListener(ridesListener);
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
        if (rideList.isEmpty()) {
            binding.ridesRecyclerView.setVisibility(View.GONE);
            binding.emptyView.setVisibility(View.VISIBLE);
        } else {
            binding.ridesRecyclerView.setVisibility(View.VISIBLE);
            binding.emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Remove the listener to prevent memory leaks
        if (ridesRef != null && ridesListener != null) {
            ridesRef.removeEventListener(ridesListener);
        }

        // Clean up the binding object when the view is destroyed to avoid memory leaks
        binding = null;
    }
}