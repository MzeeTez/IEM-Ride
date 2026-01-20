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
import com.example.iemride.databinding.FragmentMyRidesBinding;
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

public class MyRidesFragment extends Fragment {

    private FragmentMyRidesBinding binding;
    private DatabaseReference requestsRef;
    private MyRidesAdapter adapter;
    private List<RideRequest> myRequestsList;
    private ValueEventListener requestsListener;
    private String currentUserId;
    private static final String TAG = "MyRidesFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMyRidesBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        requestsRef = FirebaseDatabase.getInstance().getReference("rideRequests");

        setupRecyclerView();
        loadMyRideRequests();
    }

    private void setupRecyclerView() {
        myRequestsList = new ArrayList<>();
        adapter = new MyRidesAdapter(myRequestsList);
        binding.myRidesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.myRidesRecyclerView.setAdapter(adapter);
    }

    private void loadMyRideRequests() {
        // Query requests sent by the current user
        Query myRequestsQuery = requestsRef.orderByChild("fromUserId").equalTo(currentUserId);

        requestsListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myRequestsList.clear();

                for (DataSnapshot requestSnapshot : dataSnapshot.getChildren()) {
                    try {
                        RideRequest request = requestSnapshot.getValue(RideRequest.class);
                        if (request != null) {
                            request.setRequestId(requestSnapshot.getKey());
                            myRequestsList.add(request);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing request data: " + e.getMessage());
                    }
                }

                // Sort by timestamp (newest first)
                Collections.sort(myRequestsList, new Comparator<RideRequest>() {
                    @Override
                    public int compare(RideRequest r1, RideRequest r2) {
                        Long timestamp1 = getTimestampAsLong(r1.getTimestamp());
                        Long timestamp2 = getTimestampAsLong(r2.getTimestamp());

                        if (timestamp1 == null && timestamp2 == null) return 0;
                        if (timestamp1 == null) return 1;
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
                updateEmptyView();
            }
        };

        myRequestsQuery.addValueEventListener(requestsListener);
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
        if (myRequestsList.isEmpty()) {
            binding.myRidesRecyclerView.setVisibility(View.GONE);
            binding.emptyView.setVisibility(View.VISIBLE);
        } else {
            binding.myRidesRecyclerView.setVisibility(View.VISIBLE);
            binding.emptyView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Remove the listener to prevent memory leaks
        if (requestsRef != null && requestsListener != null) {
            requestsRef.removeEventListener(requestsListener);
        }

        binding = null;
    }
}