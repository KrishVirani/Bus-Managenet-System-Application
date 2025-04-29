package com.example.busmanagermentapplicationfinal.conductor;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.busmanagermentapplicationfinal.LoginActivity;
import com.example.busmanagermentapplicationfinal.R;
import com.google.android.gms.location.*;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ConductorBaseActivity extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final long LOCATION_UPDATE_INTERVAL = 5000;

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected Toolbar toolbar;
    TextView toolbarTitle;

    private FirebaseFirestore db;
    private static final String BUS_ID = "bmsGXGdCHZCLtShesHJ8";

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conductor_base);

        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setupLocationUpdates();
        checkPermissionsAndStartUpdates();
    }

    public void setupDrawer(int layoutResID) {
        DrawerLayout fullLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_conductor_base, null);
        FrameLayout frameLayout = fullLayout.findViewById(R.id.activity_content);
        getLayoutInflater().inflate(layoutResID, frameLayout, true);
        super.setContentView(fullLayout);

        drawerLayout = fullLayout;
        navigationView = fullLayout.findViewById(R.id.navigation_view);
        toolbar = fullLayout.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbarTitle = fullLayout.findViewById(R.id.toolbar_title);

        View headerView = navigationView.getHeaderView(0);
        TextView navHeaderRole = headerView.findViewById(R.id.nav_header_role);
//        navHeaderRole.setText("Welcome Conductor\nKrish Virani");

        db.collection("User").document("27mqfQb5ja8SoPHWxBfs").get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String firstName = documentSnapshot.getString("FirstName");
                        String lastName = documentSnapshot.getString("LastName");
                        navHeaderRole.setText("Welcome Conductor\n" + firstName + " " + lastName);
                    } else {
                        navHeaderRole.setText("Welcome Conductor\nUnknown");
                    }
                })
                .addOnFailureListener(e -> {
                    navHeaderRole.setText("Welcome Conductor\nError loading name");
                    Log.e("FirestoreError", "Failed to fetch conductor name", e);
                });


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.nav_dashboard) {
                startActivity(new Intent(this, ConductorDashbord.class));
            }
            else if (id == R.id.nav_book_ticket) {
                startActivity(new Intent(this, Conductor_TicketBooking.class));
                return true;
            }else if (id == R.id.nav_view_booking) {
                startActivity(new Intent(this, conductor_view_Check_booking.class));
            } else if (id == R.id.nav_bus_schedule) {
                startActivity(new Intent(this, Conductor_Checkschedule.class));
            } else if (id == R.id.nav_Emargency_alert) {
                startActivity(new Intent(this, create_emergency_alert.class));
            } else if (id == R.id.nav_logout) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void setupLocationUpdates() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(LOCATION_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(Priority.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        Log.d("LocationUpdate", "Lat: " + location.getLatitude() + ", Lng: " + location.getLongitude());
                        uploadLocationToFirebase(location.getLatitude(), location.getLongitude());
                    }
                }
            }
        };
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void uploadLocationToFirebase(double latitude, double longitude) {
        Map<String, Object> data = new HashMap<>();
        data.put("location", new com.google.firebase.firestore.GeoPoint(latitude, longitude));
        data.put("busId", FirebaseFirestore.getInstance().document("Bus_Details/" + BUS_ID));
        data.put("timestamp", Timestamp.now());

        db.collection("bus_locations").document(BUS_ID).set(data)
                .addOnSuccessListener(unused -> Log.d("FirebaseUpload", "Location updated"))
                .addOnFailureListener(e -> Log.e("FirebaseUpload", "Upload failed", e));
    }

    private void checkPermissionsAndStartUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            checkGPSAndStartLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void checkGPSAndStartLocationUpdates() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            Toast.makeText(this, "Please enable GPS", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        } else {
            startLocationUpdates(); // ✅ Start location updates only if GPS is ON
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkGPSAndStartLocationUpdates();
            } else {
                Toast.makeText(this, "Location permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
