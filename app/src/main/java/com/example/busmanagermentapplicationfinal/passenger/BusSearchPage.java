package com.example.busmanagermentapplicationfinal.passenger;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.busmanagermentapplicationfinal.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.concurrent.atomic.AtomicBoolean;

public class BusSearchPage extends AppCompatActivity {

    LinearLayout busListLayout;
    String from, to, date;
    String sourceStationId, destinationStationId;

    ImageView btnback3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_search_page);

        busListLayout = findViewById(R.id.busListLayout);

        from = getIntent().getStringExtra("from");
        to = getIntent().getStringExtra("to");
        date = getIntent().getStringExtra("date");
        btnback3=findViewById(R.id.imgback3);

        btnback3.setOnClickListener(v -> finish());
        fetchStationIdsAndLoadBuses(from, to); // ✅ Only called once after station IDs are fetched
    }

    private void fetchStationIdsAndLoadBuses(String fromName, String toName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("Station").get().addOnSuccessListener(snapshot -> {
            int found = 0;
            for (QueryDocumentSnapshot doc : snapshot) {
                String name = doc.getString("Name");
                String stationId = doc.getId();

                if (name != null && stationId != null) {
                    if (name.equalsIgnoreCase(fromName)) {
                        sourceStationId = stationId;
                        found++;
                    }
                    if (name.equalsIgnoreCase(toName)) {
                        destinationStationId = stationId;
                        found++;
                    }

                    if (found == 2) break;
                }
            }

            if (found == 2) {
                loadBuses();
            } else {
                Toast.makeText(this, "Unable to find station IDs for selected route.", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to fetch stations: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void loadBuses() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        busListLayout.removeAllViews(); // Clear existing views

        db.collection("Schedule")
                .whereEqualTo("ShiftDate", date)
                .get()
                .addOnSuccessListener(scheduleSnapshots -> {
                    if (scheduleSnapshots.isEmpty()) {
                        Toast.makeText(this, "No buses found on this date.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    AtomicBoolean busFound = new AtomicBoolean(false);

                    for (QueryDocumentSnapshot scheduleDoc : scheduleSnapshots) {
                        DocumentReference routeRef = scheduleDoc.getDocumentReference("routeId");
                        DocumentReference busRef = scheduleDoc.getDocumentReference("busId");
                         if (routeRef == null || busRef == null) continue;

                        String routeId = routeRef.getId();
                        String busId = busRef.getId();

                        db.collection("Stop")
                                .whereEqualTo("routeId", db.collection("Route").document(routeId))
                                .get()
                                .addOnSuccessListener(stopsSnapshot -> {
                                    int sourceOrder = -1, destOrder = -1;

                                    for (QueryDocumentSnapshot stopDoc : stopsSnapshot) {
                                        DocumentReference stationRef = stopDoc.getDocumentReference("stationId");
                                        Long stopOrderLong = stopDoc.getLong("StopOrder");
//                                        Log.d("Krish", "Stop Order: " + stopOrderLong + "____" + stationRef.getId());
//                                        Toast.makeText(this, "Stop Order: " + stopOrderLong + "____" + stationRef.getId(), Toast.LENGTH_SHORT).show();
                                        if (stationRef != null && stopOrderLong != null) {
                                            int stopOrder = stopOrderLong.intValue();
                                            String stopStationId = stationRef.getId();

                                            if (stopStationId.equals(sourceStationId)) {
                                                sourceOrder = stopOrder;
                                            }
                                            if (stopStationId.equals(destinationStationId)) {
                                                destOrder = stopOrder;
                                            }
                                        }
                                    }

                                    if (sourceOrder != -1 && destOrder != -1 && sourceOrder < destOrder) {
                                        busFound.set(true);

                                        db.collection("Bus_Details")
                                                .document(busId)
                                                .get()
                                                .addOnSuccessListener(busDoc -> {
                                                    String busName = busDoc.getString("BusName");
                                                    String type = busDoc.getString("BusType");
                                                    Double fare = busDoc.getDouble("BusFare_Fee");
                                                    String category = busDoc.getString("Category");
                                                    String dimension = busDoc.getString("Dimension");
                                                    String plateNumber = busDoc.getString("PlateNumber");
                                                    Long capacity = busDoc.getLong("Capacity");

                                                    View busCard = LayoutInflater.from(this).inflate(R.layout.item_bus, null);
                                                    ((TextView) busCard.findViewById(R.id.busName)).setText(busName != null ? busName : "Unknown Bus");
                                                    ((TextView) busCard.findViewById(R.id.busType)).setText(type != null ? type : "Unknown Type");
                                                    ((TextView) busCard.findViewById(R.id.fare)).setText("₹" + (fare != null ? fare : 0));
                                                    ((TextView) busCard.findViewById(R.id.category)).setText("Category: " + category);
                                                    ((TextView) busCard.findViewById(R.id.dimension)).setText("Layout: " + dimension);
                                                    ((TextView) busCard.findViewById(R.id.plateNumber)).setText("Plate No: " + plateNumber);
                                                    ((TextView) busCard.findViewById(R.id.capacity)).setText("Capacity: " + capacity);

                                                    // Static values for now (you can later fetch from Schedule if needed)
                                                    ((TextView) busCard.findViewById(R.id.departure)).setText("Departs: 08:00 AM");
                                                    ((TextView) busCard.findViewById(R.id.arrival)).setText("Arrives: 01:00 PM");
                                                    ((TextView) busCard.findViewById(R.id.duration)).setText("Duration: 5h");

                                                    // Handle card click → SeatSelectionPage
                                                    busCard.setOnClickListener(v -> {
                                                        Intent i = new Intent(BusSearchPage.this, SeatSelectionPage.class);
                                                        i.putExtra("busId", busId);
                                                        i.putExtra("busName", busName);
                                                        i.putExtra("busType", type);
                                                        i.putExtra("busFare", fare != null ? fare.intValue() : 0);
                                                        i.putExtra("busCategory", category);
                                                        i.putExtra("busDimension", dimension);
                                                        i.putExtra("busPlateNumber", plateNumber);
                                                        i.putExtra("busCapacity", capacity);
                                                        i.putExtra("from", from);
                                                        i.putExtra("to", to);
                                                        i.putExtra("date", date);
                                                        startActivity(i);
                                                    });

                                                    busListLayout.addView(busCard);
                                                });

                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error loading buses: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }



}
