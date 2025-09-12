package com.example.iemride;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class User {
    private String name;
    private String phoneNumber;
    private String profileImageUrl;
    private String vehicleNumber;
    private String vehicleModel;
    private double rating; // Add rating field for future use
    @ServerTimestamp
    private Date joinedDate;

    // Required empty public constructor for Firestore
    public User() {}

    public User(String name, String phoneNumber, String profileImageUrl, String vehicleNumber, String vehicleModel) {
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.profileImageUrl = profileImageUrl;
        this.vehicleNumber = vehicleNumber;
        this.vehicleModel = vehicleModel;
        this.rating = 4.5; // Default rating
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }

    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }

    public double getRating() { return rating; }
    public void setRating(double rating) { this.rating = rating; }

    public Date getJoinedDate() { return joinedDate; }
    public void setJoinedDate(Date joinedDate) { this.joinedDate = joinedDate; }
}