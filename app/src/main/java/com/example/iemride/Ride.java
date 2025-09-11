package com.example.iemride;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Ride {
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
    private @ServerTimestamp Date timestamp;

    public Ride() {}

    public Ride(String driverName, String vehicleModel, double driverRating, String departureLocation, String destination, String time, String vehicleType, int seatsAvailable, double pricePerSeat, String userId) {
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
    }

    // Getters
    public String getDriverName() { return driverName; }
    public String getVehicleModel() { return vehicleModel; }
    public double getDriverRating() { return driverRating; }
    public String getDepartureLocation() { return departureLocation; }
    public String getDestination() { return destination; }
    public String getTime() { return time; }
    public String getVehicleType() { return vehicleType; }
    public int getSeatsAvailable() { return seatsAvailable; }
    public double getPricePerSeat() { return pricePerSeat; }
    public String getUserId() { return userId; }
    public Date getTimestamp() { return timestamp; }
}