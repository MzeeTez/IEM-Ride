package com.example.iemride;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.iemride.databinding.RideItemBinding;
import java.util.List;
import java.util.Locale;

public class RideAdapter extends RecyclerView.Adapter<RideAdapter.RideViewHolder> {

    private final List<Ride> rideList;

    public RideAdapter(List<Ride> rideList) {
        this.rideList = rideList;
    }

    @NonNull
    @Override
    public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RideItemBinding binding = RideItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RideViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull RideViewHolder holder, int position) {
        Ride ride = rideList.get(position);
        holder.bind(ride);

        // Set OnClickListener for the book button
        holder.binding.bookButton.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), DetailsActivity.class);
            // Pass the entire Ride object to the DetailsActivity
            intent.putExtra("ride", ride);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return rideList.size();
    }

    static class RideViewHolder extends RecyclerView.ViewHolder {
        private final RideItemBinding binding;

        public RideViewHolder(RideItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(Ride ride) {
            // Bind Driver Info
            binding.driverNameTextView.setText(ride.getDriverName());
            binding.ratingTextView.setText(String.valueOf(ride.getDriverRating()));
            binding.vehicleModelTextView.setText(ride.getVehicleModel());
            // You can add logic to load an actual avatar image here using a library like Glide or Picasso

            // Bind Route Info
            binding.departureLocationTextView.setText(ride.getDepartureLocation());
            binding.destinationLocationTextView.setText(ride.getDestination());
            binding.departureTimeTextView.setText(ride.getTime());

            // Bind Booking Info
            binding.priceTextView.setText(String.format(Locale.getDefault(), "₹%.0f", ride.getPricePerSeat()));
            binding.seatsRemainingTextView.setText(String.format(Locale.getDefault(), "%d seats remaining", ride.getSeatsAvailable()));
        }
    }
}