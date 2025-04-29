package com.example.busmanagermentapplicationfinal.passenger;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.busmanagermentapplicationfinal.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class BusTrackingService extends Service {
    private static final String CHANNEL_ID = "BusTrackingChannel";
    private static final int NOTIFICATION_ID = 1;
    private Handler handler;
    private FirebaseFirestore db;
    private String currentPassengerId = "5g6JLcclV6q12ltu5B0P"; // Replace with actual logic to get the current passenger ID

    @SuppressLint("ForegroundServiceType")
    @Override
    public void onCreate() {
        super.onCreate();

        db = FirebaseFirestore.getInstance();

        // Create the notification channel for foreground service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Bus Tracking Service",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = (NotificationManager) getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // Set up a periodic task to check bus location every 2 minutes (120000ms)
        handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                checkBusLocationAndSendAlerts();
                handler.postDelayed(this, 120000); // 2 minutes
            }
        }, 0); // Start immediately
    }

    private void checkBusLocationAndSendAlerts() {
        // Query to fetch passenger booking details
        db.collection("OnlineTicketBooking")
                .whereEqualTo("PassangerId", db.document("Passenger/" + currentPassengerId))
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    for (QueryDocumentSnapshot doc : querySnapshots) {
                        DocumentReference busRef = doc.getDocumentReference("Bus_id");
                        DocumentReference toStationRef = doc.getDocumentReference("To");
                        if (busRef != null && toStationRef != null) {
                            // Fetch destination coordinates
                            toStationRef.get().addOnSuccessListener(toSnap -> {
                                GeoPoint location = toSnap.getGeoPoint("Location");
                                double destLat;
                                double destLng;

                                if (location != null) {
                                    destLat = location.getLatitude();
                                    destLng = location.getLongitude();

                                    Log.d("StationLocation", "Lat: " + destLat + ", Lng: " + destLng);
                                } else {
                                    destLng = 0;
                                    destLat = 0;
                                    Log.e("StationLocation", "Location field is null");
                                }

                                // Start listening to bus location
                                String busId = busRef.getId();
                                Toast.makeText(this, "Bus ID: " + busId, Toast.LENGTH_SHORT).show();
                                db.collection("bus_locations")
                                        .whereEqualTo("busId", busRef)
                                        .addSnapshotListener((querySnapshot, error) -> {
                                            Toast.makeText(this, "Bus Location: " + querySnapshot, Toast.LENGTH_SHORT).show();
                                            if (error != null) {
                                                Log.e("Alert", "Location listener error", error);
                                                return;
                                            }

                                            if (querySnapshot != null && !querySnapshot.isEmpty()) {
                                                for (QueryDocumentSnapshot doc1 : querySnapshot) {
                                                    Toast.makeText(this, "Bus Location: " + doc1.getId(), Toast.LENGTH_SHORT).show();
                                                    GeoPoint busLocation = doc1.getGeoPoint("location");
                                                    if (busLocation != null) {
                                                        double busLat = busLocation.getLatitude();
                                                        double busLng = busLocation.getLongitude();

                                                        Toast.makeText(this, "Bus Location: " + busLat + ", " + busLng, Toast.LENGTH_SHORT).show();

                                                        double distance = calculateDistance(busLat, busLng, destLat, destLng);
                                                        Toast.makeText(this, "Distance: " + distance, Toast.LENGTH_SHORT).show();

                                                        if (distance <= 100.0) { // If distance is within 1 KM
                                                            sendNotification("Your destination is near!");
                                                        }
                                                    }
                                                }
                                            } else {
                                                Log.d("BusLocation", "No matching location found for busId: " + busId);
                                            }
                                        });
                            });
                        }
                    }
                });
    }

    // Distance calculator using Haversine formula
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Earth radius in kilometers
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    // Sends a notification to the user
    private void sendNotification(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "bus_alert_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Bus Alerts", NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(true);
            channel.enableVibration(true);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.bus) // Use your own icon here
                .setContentTitle("BUS TICKETING")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(true);

        manager.notify(1, builder.build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null); // Stop the periodic task
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
