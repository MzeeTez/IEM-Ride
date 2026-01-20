package com.example.iemride;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.iemride.databinding.MyRideItemBinding;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyRidesAdapter extends RecyclerView.Adapter<MyRidesAdapter.MyRideViewHolder> {

    private final List<RideRequest> requestList;

    public MyRidesAdapter(List<RideRequest> requestList) {
        this.requestList = requestList;
    }

    @NonNull
    @Override
    public MyRideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        MyRideItemBinding binding = MyRideItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new MyRideViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyRideViewHolder holder, int position) {
        RideRequest request = requestList.get(position);
        holder.bind(request);
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    static class MyRideViewHolder extends RecyclerView.ViewHolder {
        private final MyRideItemBinding binding;

        public MyRideViewHolder(MyRideItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(RideRequest request) {
            binding.pickupLocationTextView.setText(request.getPickupLocation());
            binding.requestIdTextView.setText("Request ID: " + request.getRequestId());

            // Format timestamp
            if (request.getTimestamp() != null) {
                Long timestamp = null;
                if (request.getTimestamp() instanceof Long) {
                    timestamp = (Long) request.getTimestamp();
                } else if (request.getTimestamp() instanceof Double) {
                    timestamp = ((Double) request.getTimestamp()).longValue();
                }

                if (timestamp != null) {
                    Date date = new Date(timestamp);
                    SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
                    binding.timestampTextView.setText(sdf.format(date));
                } else {
                    binding.timestampTextView.setText("Unknown time");
                }
            } else {
                binding.timestampTextView.setText("Unknown time");
            }

            // Show status with appropriate colors
            String status = request.getStatus();
            binding.statusTextView.setText(getStatusText(status));
            binding.statusTextView.setTextColor(getStatusColor(status));

            // Set click listener for accepted requests to view on maps
            if ("accepted".equals(status)) {
                binding.getRoot().setOnClickListener(v -> {
                    Intent intent = new Intent(v.getContext(), RequestStatusActivity.class);
                    intent.putExtra("requestId", request.getRequestId());
                    intent.putExtra("rideRequest", request);
                    v.getContext().startActivity(intent);
                });
                binding.actionHintTextView.setText("Tap to view on maps");
                binding.actionHintTextView.setVisibility(android.view.View.VISIBLE);
            } else {
                binding.getRoot().setOnClickListener(null);
                if ("pending".equals(status)) {
                    binding.actionHintTextView.setText("Waiting for driver response");
                    binding.actionHintTextView.setVisibility(android.view.View.VISIBLE);
                } else {
                    binding.actionHintTextView.setVisibility(android.view.View.GONE);
                }
            }
        }

        private String getStatusText(String status) {
            switch (status) {
                case "pending":
                    return "Pending ⏳";
                case "accepted":
                    return "Accepted ✓";
                case "rejected":
                    return "Rejected ✗";
                default:
                    return "Unknown";
            }
        }

        private int getStatusColor(String status) {
            switch (status) {
                case "pending":
                    return itemView.getContext().getColor(android.R.color.holo_orange_light);
                case "accepted":
                    return itemView.getContext().getColor(android.R.color.holo_green_light);
                case "rejected":
                    return itemView.getContext().getColor(android.R.color.holo_red_light);
                default:
                    return itemView.getContext().getColor(android.R.color.darker_gray);
            }
        }
    }
}