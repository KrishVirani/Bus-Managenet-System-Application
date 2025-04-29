package com.example.busmanagermentapplicationfinal.conductor;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.busmanagermentapplicationfinal.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ConductorDashbord extends ConductorBaseActivity {
    private FrameLayout scheduleContainer;

    private FirebaseFirestore db;
    private String staticConductorId = "syG2YRgAF8h6dsCh2gRo";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_conductor_dashbord);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupDrawer(R.layout.activity_conductor_dashbord);
        // Set Toolbar title
        toolbarTitle.setText("Dashboard");

        scheduleContainer = findViewById(R.id.scheduleContainer);
        db = FirebaseFirestore.getInstance();

        loadSchedule();
    }
    private void loadSchedule() {
        DocumentReference userRef = db.collection("User").document(staticConductorId);
        db.collection("Schedule")
                .whereEqualTo("conductoreId", userRef)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    boolean hasScheduleToday = false;


                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String shiftDate = doc.getString("ShiftDate");
                        String arrivalTime = doc.getString("Arrival_time");
                        String departureTime = doc.getString("Departure_time");

                        try {
                            // Parse date as dd/MM/yyyy
                            SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
                            Date scheduleDate = sdf.parse(shiftDate);
                            Date today = sdf.parse(sdf.format(new Date()));

//
//                            Toast.makeText(getApplicationContext(), "date: today "+today +", database "+scheduleDate, Toast.LENGTH_SHORT).show();

                            // Check if schedule is today
                            if (scheduleDate != null && scheduleDate.equals(today)) {
                                hasScheduleToday = true;

                                DocumentReference busRef = doc.getDocumentReference("busId");

                                if (busRef != null) {
                                    busRef.get().addOnSuccessListener(busSnap -> {
                                        if (busSnap.exists()) {
                                            String busName = busSnap.getString("BusName");
                                            String busType = busSnap.getString("BusType");
                                            String plateNo = busSnap.getString("PlateNumber");
                                            String fare = busSnap.getString("BusFare_Fee");

                                            View scheduleView = getLayoutInflater().inflate(R.layout.schedule_card, null);

                                            TextView tvDate = scheduleView.findViewById(R.id.tvDate);
                                            TextView tvTimes = scheduleView.findViewById(R.id.tvTimes);
                                            TextView tvBusName = scheduleView.findViewById(R.id.tvBusName);
                                            TextView tvBusDetails = scheduleView.findViewById(R.id.tvBusDetails);

                                            tvDate.setText("Date: " + shiftDate);
                                            tvTimes.setText("Arrival: " + arrivalTime + " | Departure: " + departureTime);
                                            tvBusName.setText(busName + " (" + busType + ")");
                                            tvBusDetails.setText("Fare: ₹" + fare + " | Plate: " + plateNo);

                                            scheduleContainer.addView(scheduleView);
                                        }
                                    });
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // Show "No schedule" if nothing found for today
                    if (!hasScheduleToday) {
                        View noScheduleView = getLayoutInflater().inflate(R.layout.no_schedule_box, null);
                        scheduleContainer.addView(noScheduleView);
                    }
                });
    }

}