package com.example.iemride;

import com.google.firebase.database.ServerValue;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Ride implements Serializable {
    // Adding a unique ID for serialization
    private static final long serialVersionUID = 1L;

    // New Fields for Driver Info
    private String driverName;
    private String vehicleModel;
    private double driverRating;

    // Existing Fields
    private String departureLocation;
    private String destination;
    private String time;
    private String vehicleType;
    private int seatsAvailable;
    private double pricePerSeat;
    private String userId;
    private Object timestamp; // Changed to Object for Realtime Database compatibility
    private String rideId; // Add ride ID for easy reference

    public Ride() {}

    public Ride(String driverName, String vehicleModel, double driverRating, String departureLocation,
                String destination, String time, String vehicleType, int seatsAvailable,
                double pricePerSeat, String userId) {
        this.driverName = driverName;
        this.vehicleModel = vehicleModel;
        this.driverRating = driverRating;
        this.departureLocation = departureLocation;
        this.destination = destination;
        this.time = time;
        this.vehicleType = vehicleType;
        this.seatsAvailable = seatsAvailable;
        this.pricePerSeat = pricePerSeat;
        this.userId = userId;
        this.timestamp = ServerValue.TIMESTAMP; // Use Realtime Database server timestamp
    }

    // Helper method to get timestamp for saving
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("driverName", driverName);
        result.put("vehicleModel", vehicleModel);
        result.put("driverRating", driverRating);
        result.put("departureLocation", departureLocation);
        result.put("destination", destination);
        result.put("time", time);
        result.put("vehicleType", vehicleType);
        result.put("seatsAvailable", seatsAvailable);
        result.put("pricePerSeat", pricePerSeat);
        result.put("userId", userId);
        result.put("timestamp", ServerValue.TIMESTAMP);
        return result;
    }

    // Getters and Setters
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }

    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }

    public double getDriverRating() { return driverRating; }
    public void setDriverRating(double driverRating) { this.driverRating = driverRating; }

    public String getDepartureLocation() { return departureLocation; }
    public void setDepartureLocation(String departureLocation) { this.departureLocation = departureLocation; }

    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public int getSeatsAvailable() { return seatsAvailable; }
    public void setSeatsAvailable(int seatsAvailable) { this.seatsAvailable = seatsAvailable; }

    public double getPricePerSeat() { return pricePerSeat; }
    public void setPricePerSeat(double pricePerSeat) { this.pricePerSeat = pricePerSeat; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public Object getTimestamp() { return timestamp; }
    public void setTimestamp(Object timestamp) { this.timestamp = timestamp; }

    public String getRideId() { return rideId; }
    public void setRideId(String rideId) { this.rideId = rideId; }
}