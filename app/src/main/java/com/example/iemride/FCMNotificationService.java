package com.example.iemride;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FCMNotificationService extends FirebaseMessagingService {

    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "ride_requests_channel";
    private static final String CHANNEL_NAME = "Ride Requests";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            String type = remoteMessage.getData().get("type");
            if ("ride_request".equals(type)) {
                handleRideRequestNotification(remoteMessage);
            } else if ("request_status".equals(type)) {
                handleRequestStatusNotification(remoteMessage);
            }
        }

        // Check if message contains a notification payload
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
            showNotification(
                    remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody(),
                    null
            );
        }
    }

    private void handleRideRequestNotification(RemoteMessage remoteMessage) {
        String title = "New Ride Request";
        String body = remoteMessage.getData().get("passenger_name") + " wants a ride from " +
                remoteMessage.getData().get("pickup_location");

        // Intent to open RideRequestsActivity
        Intent intent = new Intent(this, RideRequestsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        showNotification(title, body, intent);
    }

    private void handleRequestStatusNotification(RemoteMessage remoteMessage) {
        String status = remoteMessage.getData().get("status");
        String title = "Ride Request " + (status.equals("accepted") ? "Accepted" : "Rejected");
        String body = status.equals("accepted") ?
                "Your ride request has been accepted! You can now access maps." :
                "Your ride request has been rejected. Try booking another ride.";

        // Intent to open appropriate activity based on status
        Intent intent;
        if ("accepted".equals(status)) {
            // For now, go to RequestStatusActivity. Later can go directly to MapsActivity
            intent = new Intent(this, RequestStatusActivity.class);
            intent.putExtra("requestId", remoteMessage.getData().get("requestId"));
        } else {
            intent = new Intent(this, HomeActivity.class);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        showNotification(title, body, intent);
    }

    private void showNotification(String title, String body, Intent intent) {
        PendingIntent pendingIntent = null;
        if (intent != null) {
            pendingIntent = PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
            );
        }

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_car_connector) // Use your car icon
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(NotificationCompat.DEFAULT_ALL);

        if (pendingIntent != null) {
            notificationBuilder.setContentIntent(pendingIntent);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "Refreshed token: " + token);

        // Send token to your server or save it locally
        sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {
        // Save FCM token to Firebase Database under user's profile
        // This will be used to send targeted notifications
        com.google.firebase.auth.FirebaseAuth auth = com.google.firebase.auth.FirebaseAuth.getInstance();
        if (auth.getCurrentUser() != null) {
            String userId = auth.getCurrentUser().getUid();
            com.google.firebase.database.FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(userId)
                    .child("fcmToken")
                    .setValue(token)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "FCM token saved successfully"))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to save FCM token", e));
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Notifications for ride requests and updates");
            channel.enableLights(true);
            channel.enableVibration(true);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}