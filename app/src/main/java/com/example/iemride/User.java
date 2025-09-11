package com.example.iemride;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class User {
    private String name;
    private String phoneNumber;
    private String profileImageUrl;
    private String vehicleNumber; // New Field
    private String vehicleModel;  // New Field
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
    }

    // Getters
    public String getName() { return name; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getProfileImageUrl() { return profileImageUrl; }
    public String getVehicleNumber() { return vehicleNumber; }
    public String getVehicleModel() { return vehicleModel; }
    public Date getJoinedDate() { return joinedDate; }
}