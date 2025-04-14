package com.example.busmanagermentapplicationfinal.conductor;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

public class Conductor_Checkschedule extends ConductorBaseActivity {

    private LinearLayout scheduleContainer;
    private FirebaseFirestore db;
    private String staticConductorId = "syG2YRgAF8h6dsCh2gRo";

    ImageView backButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_conductor_checkschedule);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupDrawer(R.layout.activity_conductor_checkschedule);
        toolbarTitle.setText("CONDUCTOR");

        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String shiftDate = doc.getString("ShiftDate");
                        String arrivalTime = doc.getString("Arrival_time");
                        String departureTime = doc.getString("Departure_time");

                        try {
                            // Parse date with "dd/MM/yy" format
                            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy");
                            Date scheduleDate = sdf.parse(shiftDate);

                            // Get today's date without time
                            Date today = sdf.parse(sdf.format(new Date()));

                            // Only show current/future schedules
                            if (scheduleDate != null && !scheduleDate.before(today)) {
                                DocumentReference busRef = doc.getDocumentReference("busId");

                                if (busRef != null) {
                                    busRef.get().addOnSuccessListener(busSnap -> {
                                        if (busSnap.exists()) {
                                            String busName = busSnap.getString("BusName");
                                            String busType = busSnap.getString("BusType");
                                            String plateNo = busSnap.getString("PlateNumber");
                                            Long fare = busSnap.getLong("BusFare_Fee");

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
                });
    }

}