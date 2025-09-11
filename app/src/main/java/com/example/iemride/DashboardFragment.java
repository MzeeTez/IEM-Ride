package com.example.iemride;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.iemride.databinding.FragmentDashboardBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private FirebaseFirestore db;
    private RideAdapter rideAdapter;
    private List<Ride> rideList;

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

        // Initialize Firestore and the ride list/adapter
        db = FirebaseFirestore.getInstance();
        rideList = new ArrayList<>();
        rideAdapter = new RideAdapter(rideList);

        // Set up the RecyclerView
        binding.ridesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.ridesRecyclerView.setAdapter(rideAdapter);

        // Load the rides from the database
        loadRides();
    }

    private void loadRides() {
        // Listen for real-time updates from the "rides" collection in Firestore
        db.collection("rides")
                .orderBy("timestamp", Query.Direction.DESCENDING) // Show newest rides first
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        // Handle potential errors
                        return;
                    }

                    if (value != null) {
                        rideList.clear(); // Clear the old list
                        // Loop through the documents and add them to the list
                        for (QueryDocumentSnapshot doc : value) {
                            rideList.add(doc.toObject(Ride.class));
                        }
                        rideAdapter.notifyDataSetChanged(); // Refresh the list in the UI
                    }

                    // Show or hide the "empty" message based on whether the list is empty
                    if (rideList.isEmpty()) {
                        binding.ridesRecyclerView.setVisibility(View.GONE);
                        binding.emptyView.setVisibility(View.VISIBLE);
                    } else {
                        binding.ridesRecyclerView.setVisibility(View.VISIBLE);
                        binding.emptyView.setVisibility(View.GONE);
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up the binding object when the view is destroyed to avoid memory leaks
        binding = null;
    }
}