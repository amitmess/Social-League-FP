package com.example.social_league_fp.ui.location;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.social_league_fp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.util.Locale;

/**
 * Handles device GPS location tracking, requests runtime permissions, retrieves current user coordinates,
 * calculates the geodesic distance to the match stadium, and launches an external maps application.
 */
public class StadiumLocationActivity extends AppCompatActivity {

    private static final String TAG = "StadiumLocationActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseAnalytics mFirebaseAnalytics;

    private TextView tvStadiumName;
    private TextView tvStadiumCoords;
    private TextView tvUserCoords;
    private TextView tvDistance;
    private ProgressBar progressBarLocation;
    private MaterialButton btnGetDirections;
    private MaterialToolbar toolbar;

    private String matchId;
    private String stadiumName;
    private double stadiumLat = 0.0;
    private double stadiumLng = 0.0;
    private boolean hasStadiumCoords = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stadium_location);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        // Read target stadium location coordinates and label passed from details intent.
        Intent intent = getIntent();
        matchId = intent.getStringExtra("extra_match_id");
        stadiumName = intent.getStringExtra("extra_location");
        if (stadiumName == null) stadiumName = "Stadium Venue";

        if (intent.hasExtra("extra_latitude") && intent.hasExtra("extra_longitude")) {
            stadiumLat = intent.getDoubleExtra("extra_latitude", 0.0);
            stadiumLng = intent.getDoubleExtra("extra_longitude", 0.0);
            hasStadiumCoords = true;
        }

        // Track GPS location screen load metrics.
        Bundle bundle = new Bundle();
        bundle.putString("screen_name", "stadium_location");
        bundle.putString("match_id", matchId);
        mFirebaseAnalytics.logEvent("open_location", bundle);

        initViews();
        setupToolbar();
        displayStadiumData();

        // Initiate GPS flow only if stadium coordinates are configured.
        if (hasStadiumCoords) {
            checkLocationPermissions();
        } else {
            tvStadiumCoords.setText("Stadium Location: Coordinates not configured");
            tvUserCoords.setText("Unable to calculate distance without stadium coordinates.");
            tvDistance.setText("Distance: --");
        }
    }

    private void initViews() {
        tvStadiumName = findViewById(R.id.tvStadiumName);
        tvStadiumCoords = findViewById(R.id.tvStadiumCoords);
        tvUserCoords = findViewById(R.id.tvUserCoords);
        tvDistance = findViewById(R.id.tvDistance);
        progressBarLocation = findViewById(R.id.progressBarLocation);
        btnGetDirections = findViewById(R.id.btnGetDirections);

        btnGetDirections.setOnClickListener(v -> launchMapsIntent());
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    private void displayStadiumData() {
        tvStadiumName.setText(stadiumName);
        if (hasStadiumCoords) {
            tvStadiumCoords.setText(String.format(Locale.getDefault(), "Stadium Location: %.5f, %.5f", stadiumLat, stadiumLng));
        }
    }

    // Verify whether fine location permission is already granted; request it if not.
    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            logPermissionEvent("granted");
            getUserLocation();
        } else {
            // Request runtime location permissions from the user.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                logPermissionEvent("granted");
                getUserLocation();
            } else {
                // Graceful degradation: Log permission denial, inform user, and disable distance updates.
                logPermissionEvent("denied");
                Toast.makeText(this, "Location permission denied. Cannot calculate distance.", Toast.LENGTH_LONG).show();
                tvUserCoords.setText("Permission denied. Enable location services in settings.");
            }
        }
    }

    // Acquire current GPS coordinates using the Google Play Services Location API.
    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        progressBarLocation.setVisibility(View.VISIBLE);
        tvUserCoords.setText("Acquiring GPS lock...");

        // Request highly accurate current location update with cancellation token.
        CancellationTokenSource cts = new CancellationTokenSource();
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.getToken())
                .addOnSuccessListener(this, location -> {
                    progressBarLocation.setVisibility(View.GONE);
                    if (location != null) {
                        updateUserLocation(location);
                    } else {
                        // Fallback: Attempt to fetch the last cached location if current is null.
                        fusedLocationClient.getLastLocation().addOnSuccessListener(this, lastLocation -> {
                            if (lastLocation != null) {
                                updateUserLocation(lastLocation);
                            } else {
                                tvUserCoords.setText("Location unavailable. Make sure GPS is enabled.");
                                Toast.makeText(this, "Failed to get current location", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .addOnFailureListener(this, e -> {
                    progressBarLocation.setVisibility(View.GONE);
                    Log.e(TAG, "Failed to get location", e);
                    // Report GPS hardware / system failures to Crashlytics for monitoring.
                    FirebaseCrashlytics.getInstance().recordException(e);
                    tvUserCoords.setText("Error acquiring location: " + e.getMessage());
                });
    }

    // Calculate the geodesic distance between user and stadium coordinates in kilometers.
    private void updateUserLocation(Location location) {
        double userLat = location.getLatitude();
        double userLng = location.getLongitude();

        tvUserCoords.setText(String.format(Locale.getDefault(), "Your Location: %.5f, %.5f", userLat, userLng));

        // Use Android Location math API to compute the distance in meters.
        float[] results = new float[1];
        Location.distanceBetween(userLat, userLng, stadiumLat, stadiumLng, results);
        float distanceInMeters = results[0];
        float distanceInKm = distanceInMeters / 1000f;

        tvDistance.setText(String.format(Locale.getDefault(), "Distance: %.2f km", distanceInKm));
        btnGetDirections.setEnabled(true);
    }

    // Launch Google Maps via implicit Intent for turn-by-turn navigation guidance.
    private void launchMapsIntent() {
        Uri gmmIntentUri = Uri.parse("geo:" + stadiumLat + "," + stadiumLng + "?q=" + Uri.encode(stadiumName));
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        // Track maps launch telemetry.
        Bundle anaBundle = new Bundle();
        anaBundle.putString("match_id", matchId);
        anaBundle.putString("stadium_name", stadiumName);
        mFirebaseAnalytics.logEvent("maps_navigation_launched", anaBundle);

        if (mapIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(mapIntent);
        } else {
            // Fallback: Open google maps search URL in standard web browser.
            String url = String.format(Locale.US, "https://www.google.com/maps/search/?api=1&query=%f,%f", stadiumLat, stadiumLng);
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(webIntent);
        }
    }

    // Track GPS permissions grant/denial trends.
    private void logPermissionEvent(String result) {
        Bundle bundle = new Bundle();
        bundle.putString("permission_type", "location");
        bundle.putString("result", result);
        mFirebaseAnalytics.logEvent("gps_permission", bundle);
    }
}
