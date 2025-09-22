package com.example.iemride;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NotificationHelper {

    private static final String TAG = "NotificationHelper";
    private static final String FCM_SERVER_KEY = "BL64ng5CdmnIyArXVMKt--4bssAiQuSIig-vnd8cXKDoHLppKD7G7FXQZ8ur7PJJ7VBcS8Drxk87LiC4_DMDVjg"; // Replace with your key
    private static final String FCM_URL = "https://fcm.googleapis.com/fcm/send";

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final OkHttpClient client = new OkHttpClient();

    /**
     * Send notification when a new ride request is received
     */
    public static void sendRideRequestNotification(String driverUserId, String passengerName,
                                                   String pickupLocation, String requestId) {

        // Get driver's FCM token from Firebase
        FirebaseDatabase.getInstance().getReference("users")
                .child(driverUserId)
                .child("fcmToken")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String fcmToken = snapshot.getValue(String.class);
                        if (fcmToken != null) {
                            sendFCMNotification(fcmToken, createRideRequestPayload(passengerName, pickupLocation, requestId));
                        } else {
                            Log.w(TAG, "FCM token not found for driver: " + driverUserId);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error getting FCM token", error.toException());
                    }
                });
    }

    /**
     * Send notification when request status changes (accepted/rejected)
     */
    public static void sendRequestStatusNotification(String passengerUserId, String status, String requestId) {

        // Get passenger's FCM token from Firebase
        FirebaseDatabase.getInstance().getReference("users")
                .child(passengerUserId)
                .child("fcmToken")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String fcmToken = snapshot.getValue(String.class);
                        if (fcmToken != null) {
                            sendFCMNotification(fcmToken, createStatusUpdatePayload(status, requestId));
                        } else {
                            Log.w(TAG, "FCM token not found for passenger: " + passengerUserId);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "Error getting FCM token", error.toException());
                    }
                });
    }

    private static JSONObject createRideRequestPayload(String passengerName, String pickupLocation, String requestId) {
        try {
            JSONObject payload = new JSONObject();

            // Notification payload (shows in notification bar)
            JSONObject notification = new JSONObject();
            notification.put("title", "New Ride Request");
            notification.put("body", passengerName + " wants a ride from " + pickupLocation);

            // Data payload (handled by app when opened)
            JSONObject data = new JSONObject();
            data.put("type", "ride_request");
            data.put("passenger_name", passengerName);
            data.put("pickup_location", pickupLocation);
            data.put("requestId", requestId);

            payload.put("notification", notification);
            payload.put("data", data);

            return payload;

        } catch (JSONException e) {
            Log.e(TAG, "Error creating ride request payload", e);
            return new JSONObject();
        }
    }

    private static JSONObject createStatusUpdatePayload(String status, String requestId) {
        try {
            JSONObject payload = new JSONObject();

            // Notification payload
            JSONObject notification = new JSONObject();
            notification.put("title", "Ride Request " + (status.equals("accepted") ? "Accepted" : "Rejected"));
            notification.put("body", status.equals("accepted") ?
                    "Your ride request has been accepted! You can now access maps." :
                    "Your ride request has been rejected. Try booking another ride.");

            // Data payload
            JSONObject data = new JSONObject();
            data.put("type", "request_status");
            data.put("status", status);
            data.put("requestId", requestId);

            payload.put("notification", notification);
            payload.put("data", data);

            return payload;

        } catch (JSONException e) {
            Log.e(TAG, "Error creating status update payload", e);
            return new JSONObject();
        }
    }

    private static void sendFCMNotification(String fcmToken, JSONObject payload) {
        try {
            JSONObject message = new JSONObject();
            message.put("to", fcmToken);
            message.put("notification", payload.getJSONObject("notification"));
            message.put("data", payload.getJSONObject("data"));

            RequestBody body = RequestBody.create(message.toString(), JSON);
            Request request = new Request.Builder()
                    .url(FCM_URL)
                    .post(body)
                    .addHeader("Authorization", "key=" + FCM_SERVER_KEY)
                    .addHeader("Content-Type", "application/json")
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e(TAG, "Failed to send FCM notification", e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        Log.d(TAG, "FCM notification sent successfully");
                    } else {
                        Log.e(TAG, "FCM notification failed: " + response.body().string());
                    }
                }
            });

        } catch (JSONException e) {
            Log.e(TAG, "Error creating FCM request", e);
        }
    }
}