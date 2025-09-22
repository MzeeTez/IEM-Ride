package com.example.iemride;

import com.google.firebase.database.ServerValue;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RideRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String requestId;
    private String fromUserId;
    private String fromUserName;
    private String fromUserPhone;
    private String toDriverId;
    private String pickupLocation;
    private String status; // "pending", "accepted", "rejected"
    private Object timestamp;
    private String rideId; // Reference to the original ride

    public RideRequest() {}

    public RideRequest(String fromUserId, String fromUserName, String fromUserPhone,
                       String toDriverId, String pickupLocation, String rideId) {
        this.fromUserId = fromUserId;
        this.fromUserName = fromUserName;
        this.fromUserPhone = fromUserPhone;
        this.toDriverId = toDriverId;
        this.pickupLocation = pickupLocation;
        this.rideId = rideId;
        this.status = "pending";
        this.timestamp = ServerValue.TIMESTAMP;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("fromUserId", fromUserId);
        result.put("fromUserName", fromUserName);
        result.put("fromUserPhone", fromUserPhone);
        result.put("toDriverId", toDriverId);
        result.put("pickupLocation", pickupLocation);
        result.put("rideId", rideId);
        result.put("status", status);
        result.put("timestamp", ServerValue.TIMESTAMP);
        return result;
    }

    // Getters and Setters
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getFromUserId() { return fromUserId; }
    public void setFromUserId(String fromUserId) { this.fromUserId = fromUserId; }

    public String getFromUserName() { return fromUserName; }
    public void setFromUserName(String fromUserName) { this.fromUserName = fromUserName; }

    public String getFromUserPhone() { return fromUserPhone; }
    public void setFromUserPhone(String fromUserPhone) { this.fromUserPhone = fromUserPhone; }

    public String getToDriverId() { return toDriverId; }
    public void setToDriverId(String toDriverId) { this.toDriverId = toDriverId; }

    public String getPickupLocation() { return pickupLocation; }
    public void setPickupLocation(String pickupLocation) { this.pickupLocation = pickupLocation; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Object getTimestamp() { return timestamp; }
    public void setTimestamp(Object timestamp) { this.timestamp = timestamp; }

    public String getRideId() { return rideId; }
    public void setRideId(String rideId) { this.rideId = rideId; }
}