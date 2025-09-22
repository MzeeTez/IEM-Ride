package com.example.iemride;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.iemride.databinding.RideRequestItemBinding;
import java.util.List;

public class RideRequestsAdapter extends RecyclerView.Adapter<RideRequestsAdapter.RequestViewHolder> {

    private final List<RideRequest> requestList;
    private final OnRequestActionListener listener;

    public interface OnRequestActionListener {
        void onAcceptRequest(RideRequest request);
        void onRejectRequest(RideRequest request);
    }

    public RideRequestsAdapter(List<RideRequest> requestList, OnRequestActionListener listener) {
        this.requestList = requestList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RideRequestItemBinding binding = RideRequestItemBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new RequestViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        RideRequest request = requestList.get(position);
        holder.bind(request, listener);
    }

    @Override
    public int getItemCount() {
        return requestList.size();
    }

    static class RequestViewHolder extends RecyclerView.ViewHolder {
        private final RideRequestItemBinding binding;

        public RequestViewHolder(RideRequestItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(RideRequest request, OnRequestActionListener listener) {
            binding.passengerNameTextView.setText(request.getFromUserName());
            binding.phoneNumberTextView.setText(request.getFromUserPhone());
            binding.pickupLocationTextView.setText(request.getPickupLocation());

            // Show status
            String status = request.getStatus();
            switch (status) {
                case "pending":
                    binding.statusTextView.setText("Pending");
                    binding.statusTextView.setTextColor(itemView.getContext().getColor(android.R.color.holo_orange_light));
                    binding.acceptButton.setVisibility(android.view.View.VISIBLE);
                    binding.rejectButton.setVisibility(android.view.View.VISIBLE);
                    break;
                case "accepted":
                    binding.statusTextView.setText("Accepted ✓");
                    binding.statusTextView.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_light));
                    binding.acceptButton.setVisibility(android.view.View.GONE);
                    binding.rejectButton.setVisibility(android.view.View.GONE);
                    break;
                case "rejected":
                    binding.statusTextView.setText("Rejected ✗");
                    binding.statusTextView.setTextColor(itemView.getContext().getColor(android.R.color.holo_red_light));
                    binding.acceptButton.setVisibility(android.view.View.GONE);
                    binding.rejectButton.setVisibility(android.view.View.GONE);
                    break;
            }

            // Set button click listeners
            binding.acceptButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAcceptRequest(request);
                }
            });

            binding.rejectButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRejectRequest(request);
                }
            });
        }
    }
}