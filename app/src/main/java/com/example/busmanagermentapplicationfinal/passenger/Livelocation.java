package com.example.busmanagermentapplicationfinal.passenger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.busmanagermentapplicationfinal.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

public class Livelocation extends AppCompatActivity {

    private static final int UPDATE_INTERVAL = 5000; // 5 seconds
    private MapView mapView;
    private FirebaseFirestore db;
    private Handler handler = new Handler();

    private LinearLayout loaderContainer;
    boolean mapload = false;
    String busId, busName, plateNumber;

    private List<Marker> busMarkers = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(getApplicationContext(), getSharedPreferences("osm_prefs", Context.MODE_PRIVATE));
        setContentView(R.layout.activity_livelocation);
//        setContentLayout(R.layout.activity_passanger_dashbord);
//        toolbarTitle.setText("Live Location");

        mapView = findViewById(R.id.map);
        loaderContainer = findViewById(R.id.loaderContainer);
        mapView.setVisibility(View.INVISIBLE); // hide at first

        mapView.setTileSource(TileSourceFactory.MAPNIK);  // Best detailed view

        // ✅ Improve Tile Quality
        mapView.setTilesScaledToDpi(true);
        mapView.setFlingEnabled(true);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(7);
        mapView.setMaxZoomLevel(22.0);
        mapView.setUseDataConnection(true);
        mapView.getTileProvider().clearTileCache();
        Configuration.getInstance().setTileFileSystemCacheMaxBytes(200 * 1024 * 1024); // 200MB Cache
        Configuration.getInstance().setCacheMapTileCount((short) 20);
        Configuration.getInstance().setCacheMapTileOvershoot((short) 20);

        // ✅ Remove Tile Blurriness
        mapView.getOverlayManager().getTilesOverlay().setLoadingBackgroundColor(Color.TRANSPARENT);
        mapView.getOverlayManager().getTilesOverlay().setLoadingLineColor(Color.TRANSPARENT);

        db = FirebaseFirestore.getInstance();

        busId = getIntent().getStringExtra("busId");
        busName = getIntent().getStringExtra("busName");
        plateNumber = getIntent().getStringExtra("plateNumber");

//        showUserLocationOnMap();
        // Start fetching location every 5 seconds
        handler.postDelayed(locationUpdateRunnable, UPDATE_INTERVAL);
    }
//    @SuppressLint("MissingPermission")
//    private void showUserLocationOnMap() {
//        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
//        fusedLocationClient.getLastLocation()
//                .addOnSuccessListener(location -> {
//                    if (location != null) {
//                        double latitude = location.getLatitude();
//                        double longitude = location.getLongitude();
//
//                        GeoPoint userPoint = new GeoPoint(latitude, longitude);
//                        Marker userMarker = new Marker(mapView);
//                        userMarker.setPosition(userPoint);
//                        userMarker.setTitle("You are here");
//                        userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//                        mapView.getOverlays().add(userMarker);
//                        mapView.getController().animateTo(userPoint);
//                        mapView.invalidate();
//                    }
//                });
//    }

    private final Runnable locationUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            fetchBusLocationsAndDisplay();
            handler.postDelayed(this, UPDATE_INTERVAL);
        }
    };

    private void fetchBusLocationsAndDisplay() {

        db.collection("bus_locations")
                .whereEqualTo("busId", db.document("Bus_Details/" + busId))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (mapload == false) {
                        mapView.setVisibility(View.VISIBLE);  // show map
                        loaderContainer.setVisibility(View.GONE); // hide loader
                        mapload = true;
                    }

                    // Clear old markers
                    for (Marker marker : busMarkers) {
                        mapView.getOverlays().remove(marker);
                    }
                    busMarkers.clear();

                    boolean firstLocation = true;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        com.google.firebase.firestore.GeoPoint geoPoint = document.getGeoPoint("location");
                        if (geoPoint != null) {
                            mapView.getController().setZoom(17);
                            mapView.setMaxZoomLevel(22.0);

                            Drawable icon = ContextCompat.getDrawable(this, R.drawable.busstation);
                            icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());

                            Bitmap bitmap = ((BitmapDrawable) icon).getBitmap();
                            Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 50, 50, false);
                            Drawable iconedited = new BitmapDrawable(getResources(), scaledBitmap);

                            GeoPoint busLocation = new GeoPoint(geoPoint.getLatitude(), geoPoint.getLongitude());
                            Marker busMarker = new Marker(mapView);
                            busMarker.setPosition(busLocation);
                            busMarker.setIcon(iconedited);

                            busMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            busMarker.setTitle("Your Bus is Here \nName: " + busName + " \n Plate Number: " + plateNumber + "");

                            // Optional: Use custom bus icon
                            // busMarker.setIcon(getResources().getDrawable(R.drawable.bus_icon));

                            mapView.getOverlays().add(busMarker);
                            busMarkers.add(busMarker);

                            if (firstLocation) {
                                mapView.getController().animateTo(busLocation); // Move to first bus
                                firstLocation = false;
                            }
                        }
                    }

                    mapView.invalidate();
                    loaderContainer.setVisibility(View.GONE); // Hide loader
                })
                .addOnFailureListener(e -> {
                    loaderContainer.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to fetch bus locations", Toast.LENGTH_SHORT).show();
                });
    }
}
