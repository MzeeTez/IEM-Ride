package com.example.iemride;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.example.iemride.databinding.ActivityMapsBinding;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MapsActivity extends AppCompatActivity implements LocationListener {

    private ActivityMapsBinding binding;
    private MapView mapView;
    private LocationManager locationManager;
    private MyLocationNewOverlay myLocationOverlay;
    private IMapController mapController;

    private RideRequest currentRequest;
    private String requestId;
    private boolean isDriver;

    private static final int LOCATION_PERMISSION_REQUEST = 100;
    private static final String TAG = "MapsActivity";

    // Kolkata area coordinates (default center)
    private static final double DEFAULT_LAT = 22.5726;
    private static final double DEFAULT_LON = 88.3639;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize OSMDroid configuration
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue("IEM_Ride");

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get data from intent
        requestId = getIntent().getStringExtra("requestId");
        currentRequest = (RideRequest) getIntent().getSerializableExtra("rideRequest");
        isDriver = getIntent().getBooleanExtra("isDriver", false);

        if (requestId == null || currentRequest == null) {
            Toast.makeText(this, "Invalid request data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupUI();
        setupMap();
        checkLocationPermission();

        binding.backButton.setOnClickListener(v -> finish());
    }

    private void setupUI() {
        binding.titleTextView.setText(isDriver ? "Navigate to Pickup" : "Track Your Ride");
        binding.statusTextView.setText("Ride in Progress");
        binding.pickupLocationTextView.setText(currentRequest.getPickupLocation());
        binding.passengerDriverTextView.setText(isDriver ?
                "Passenger: " + currentRequest.getFromUserName() :
                "Driver: " + currentRequest.getFromUserName());
    }

    private void setupMap() {
        mapView = binding.mapView;
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(true);

        mapController = mapView.getController();
        mapController.setZoom(15);

        // Set default location (Kolkata)
        GeoPoint startPoint = new GeoPoint(DEFAULT_LAT, DEFAULT_LON);
        mapController.setCenter(startPoint);

        // Add location overlay
        myLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
        myLocationOverlay.enableMyLocation();
        mapView.getOverlays().add(myLocationOverlay);

        // Add markers for pickup and destination (placeholder coordinates)
        addPickupMarker();
        addDestinationMarker();
    }

    private void addPickupMarker() {
        // For demo purposes, using coordinates near Kolkata
        // In real implementation, you'd geocode the pickup location
        GeoPoint pickupPoint = new GeoPoint(22.5726, 88.3639); // IEM College area

        Marker pickupMarker = new Marker(mapView);
        pickupMarker.setPosition(pickupPoint);
        pickupMarker.setTitle("Pickup Location");
        pickupMarker.setSnippet(currentRequest.getPickupLocation());
        pickupMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_location_on));

        mapView.getOverlays().add(pickupMarker);
    }

    private void addDestinationMarker() {
        // Demo destination coordinates
        GeoPoint destPoint = new GeoPoint(22.5826, 88.3739); // Slightly different location

        Marker destMarker = new Marker(mapView);
        destMarker.setPosition(destPoint);
        destMarker.setTitle("Destination");
        destMarker.setSnippet("Ride Destination");
        destMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_car_connector));

        mapView.getOverlays().add(destMarker);
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        } else {
            startLocationUpdates();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Start GPS location updates
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    5000, 10, this);
        }

        // Start network location updates as backup
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    5000, 10, this);
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.d(TAG, "Location updated: " + location.getLatitude() + ", " + location.getLongitude());

        // Update map center to current location
        GeoPoint currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
        mapController.animateTo(currentLocation);

        // Here you can add logic to:
        // 1. Update driver's location in Firebase for passenger tracking
        // 2. Calculate ETA
        // 3. Show route to pickup/destination
        updateLocationInFirebase(location);
    }

    private void updateLocationInFirebase(Location location) {
        // Update current user's location in Firebase for real-time tracking
        String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();

        java.util.HashMap<String, Object> locationData = new java.util.HashMap<>();
        locationData.put("latitude", location.getLatitude());
        locationData.put("longitude", location.getLongitude());
        locationData.put("timestamp", System.currentTimeMillis());

        com.google.firebase.database.FirebaseDatabase.getInstance()
                .getReference("activeRides")
                .child(requestId)
                .child(isDriver ? "driverLocation" : "passengerLocation")
                .setValue(locationData)
                .addOnFailureListener(e -> Log.e(TAG, "Failed to update location", e));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    @Override
    public void onProviderEnabled(@NonNull String provider) {}

    @Override
    public void onProviderDisabled(@NonNull String provider) {}

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) {
            mapView.onPause();
        }

        // Stop location updates to save battery
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }
}