package com.example.busmanagermentapplicationfinal.passenger;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.busmanagermentapplicationfinal.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Live_Location_BusList extends PassengerBaseActivity {
    private LinearLayout busCardContainer;
    private FirebaseFirestore db;
    private static final int LOCATION_PERMISSION_REQUEST = 1;

    // Fields to store selected bus info

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setContentLayout(R.layout.activity_live_location_bus_list);
        toolbarTitle.setText("Live Location");
//        bottomNavigationView.setSelectedItemId(R.id.nav_livelocation);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        busCardContainer = findViewById(R.id.busCardContainer); // defined in XML
        db = FirebaseFirestore.getInstance();

        loadAllBuses();
    }

    private void loadAllBuses() {
        db.collection("bus_locations")
                .get()
                .addOnSuccessListener(querySnapshots -> {
                    for (DocumentSnapshot locationDoc : querySnapshots) {
                        DocumentReference busRef = locationDoc.getDocumentReference("busId");

                        if (busRef != null) {
                            busRef.get()
                                    .addOnSuccessListener(busDoc -> {
                                        if (busDoc.exists()) {
                                            String busName = busDoc.getString("BusName");
                                            String plateNumber = busDoc.getString("PlateNumber");
                                            String busId = busRef.getId();

                                            View card = createBusCard(busName, plateNumber, busId);
                                            busCardContainer.addView(card);
                                        }
                                    })
                                    .addOnFailureListener(Throwable::printStackTrace);
                        }
                    }
                })
                .addOnFailureListener(Throwable::printStackTrace);
    }

    private View createBusCard(String name, String plate, String busId) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(32, 32, 32, 32);
        card.setBackgroundResource(R.drawable.white_rounded_card);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 32);
        card.setLayoutParams(params);

        TextView tvName = new TextView(this);
        tvName.setText("Bus Name: " + name);
        tvName.setTextSize(16);
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView tvPlate = new TextView(this);
        tvPlate.setText("Plate Number: " + plate);
        tvPlate.setTextSize(14);

        card.addView(tvName);
        card.addView(tvPlate);

        card.setOnClickListener(v -> {
            Intent intent = new Intent(Live_Location_BusList.this, Livelocation.class);
            intent.putExtra("busId", busId);
            intent.putExtra("busName", name);
            intent.putExtra("plateNumber", plate);
            startActivity(intent);
        });

        return card;
    }



//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
//                                           @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == LOCATION_PERMISSION_REQUEST) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                checkGPSAndStartLocationUpdates();
//            } else {
//                Toast.makeText(this, "Location permission is required to show your position", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    private void checkGPSAndStartLocationUpdates() {
//        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            Toast.makeText(this, "Please enable GPS", Toast.LENGTH_LONG).show();
//            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
//        } else {
//            openLiveLocationActivity(); // ✅ Start location updates only if GPS is ON
//        }
//    }
}
