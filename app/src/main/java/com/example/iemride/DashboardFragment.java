package com.example.iemride;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflates the layout for this fragment using view binding
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

        // Load the rides from the database
        loadRides();
    }

    private void loadRides() {
        // Create a query to get rides ordered by timestamp (most recent first)
        Query ridesQuery = ridesRef.orderByChild("timestamp");

        ridesListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                rideList.clear();

                for (DataSnapshot rideSnapshot : dataSnapshot.getChildren()) {
                    try {
                        Ride ride = rideSnapshot.getValue(Ride.class);
                        if (ride != null) {
                            // Set the ride ID from the key
                            ride.setRideId(rideSnapshot.getKey());
                            rideList.add(ride);
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