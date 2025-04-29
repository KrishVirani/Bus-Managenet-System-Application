package com.example.busmanagermentapplicationfinal.passenger;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.busmanagermentapplicationfinal.R;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SeatSelectionPage extends AppCompatActivity {

    ImageView btnBack2;
    GridLayout gridSeats;
    TextView totalFareText;
    int seatPrice = 0;
    int selectedSeats = 0;
    int capacity = 0;
    int totalFare = 0;
    int left = 2;
    int right = 3;

    FirebaseFirestore db;
    int capacityInt, fareInt;

    String busId, busName, busType, busCategory, plateNumber, layout, fromStationId, toStationId, selectedDate, scheduleId, passengerId = "7NTNWfsOaHUnHNdagUTg";

    List<String> selectedSeatLabels = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_selection_page);

        gridSeats = findViewById(R.id.gridSeats);
        totalFareText = findViewById(R.id.totalFareText);
        db = FirebaseFirestore.getInstance();

        TextView tvBusName = findViewById(R.id.tvBusName);
        TextView tvBusType = findViewById(R.id.tvBusType);
        TextView tvBusFare = findViewById(R.id.tvBusFare);
        TextView tvPlateNumber = findViewById(R.id.tvPlateNumber);
        TextView tvArrival = findViewById(R.id.tvArrival);
        TextView tvDeparture = findViewById(R.id.tvDeparture);
        TextView tvDuration = findViewById(R.id.tvDuration);
        TextView tvFromTo = findViewById(R.id.tvFromTo);
        TextView tvDate = findViewById(R.id.tvDate);
        btnBack2 = findViewById(R.id.backButton11);
        btnBack2.setOnClickListener(v -> finish());

        Intent extras = getIntent();
        if (extras != null) {
            busId = extras.getStringExtra("busId");
            seatPrice = extras.getIntExtra("busFare", 0);
            capacity = extras.getIntExtra("busCapacity", 0);
            busName = extras.getStringExtra("busName");
            busType = extras.getStringExtra("busType");
            busCategory = extras.getStringExtra("busCategory");
            layout = extras.getStringExtra("busDimension");
            plateNumber = extras.getStringExtra("busPlateNumber");
            fromStationId = extras.getStringExtra("from");
            toStationId = extras.getStringExtra("to");
            selectedDate = extras.getStringExtra("date");
            scheduleId = extras.getStringExtra("scheduleId");

            String fromStationName = extras.getStringExtra("from");
            String toStationName = extras.getStringExtra("to");

            fetchStationIdFromName(fromStationName, true);
            fetchStationIdFromName(toStationName, false);

            tvBusName.setText(busName);
            tvBusType.setText(busType);
            tvBusFare.setText("₹" + seatPrice);
            tvPlateNumber.setText(plateNumber);
            tvArrival.setText("1:30 PM");
            tvDeparture.setText("5:50 PM");
            tvDuration.setText("5h 20m");
            tvFromTo.setText(fromStationName + " to " + toStationName);
            tvDate.setText(selectedDate);
           }

        totalFareText.setOnClickListener(v -> {
            if (selectedSeatLabels.isEmpty()) {
                Toast.makeText(this, "Please select seats", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(SeatSelectionPage.this, add_passenger_details_Page.class);
                intent.putStringArrayListExtra("selectedSeats", new ArrayList<>(selectedSeatLabels));
                intent.putExtra("totalFare", totalFare);
                intent.putExtra("seatPrice", seatPrice);
                intent.putExtra("busId", busId);
                intent.putExtra("scheduleId", scheduleId);
                intent.putExtra("fromStationId", fromStationId);
                intent.putExtra("toStationId", toStationId);
                intent.putExtra("date", selectedDate);
                intent.putExtra("passengerId", passengerId);
                startActivity(intent);
            }

        });

        fetchSeatLayoutFromFirebase();
    }

    private void fetchStationIdFromName(String stationName, boolean isFrom) {
        db.collection("Station")
                .whereEqualTo("Name", stationName)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String stationId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        if (isFrom) {
                            fromStationId = stationId;
                        } else {
                            toStationId = stationId;
                        }
                    }
                });
    }

    private void fetchSeatLayoutFromFirebase() {
        db.collection("Bus_Details").document(busId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                layout = documentSnapshot.getString("Dimension");
                capacityInt = Integer.parseInt(documentSnapshot.getString("Capacity"));
                fareInt = Integer.parseInt(documentSnapshot.getString("BusFare_Fee"));

                if (layout != null && layout.contains("x")) {
                    String[] parts = layout.split("x");
                    left = Integer.parseInt(parts[0]);
                    right = Integer.parseInt(parts[1]);
                }

                int columns = left + 1 + right + 2;
                gridSeats.setColumnCount(columns);
                renderSeats(left, right, columns);
            }
        });
    }

    private void renderSeats(int left, int right, int totalColumns) {
        int totalSeats = capacityInt;
        int seatsPerRow = left + right;
        int rows = (int) Math.ceil((double) totalSeats / seatsPerRow);
        int seatNumber = 1;

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int itemSize = screenWidth / (left + right + 4);
        ImageView steeringWheel = new ImageView(this);
        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
        params.width = itemSize;
        params.height = itemSize;
        params.columnSpec = GridLayout.spec(totalColumns - 1); // Rightmost column
        params.rowSpec = GridLayout.spec(0); // First row
        steeringWheel.setLayoutParams(params);
        steeringWheel.setImageResource(R.drawable.steering_wheel); // steering.png or .webp in drawable
        steeringWheel.setScaleType(ImageView.ScaleType.FIT_CENTER);
        gridSeats.addView(steeringWheel);

        Map<String, LinearLayout> seatMap = new HashMap<>();

        for (int i = 0; i < rows; i++) {
            for (int col = 0; col < totalColumns; col++) {
                if (col == left || col == left + 1 + right || col == left + 1) {
                    View gap = new View(this);
                    gap.setLayoutParams(new ViewGroup.LayoutParams(16, itemSize));
                    gridSeats.addView(gap);
                    continue;
                }

                if (seatNumber > totalSeats) break;

                LinearLayout seatContainer = new LinearLayout(this);
                seatContainer.setOrientation(LinearLayout.VERTICAL);
                seatContainer.setGravity(Gravity.CENTER);

                ImageView seatImage = new ImageView(this);
                seatImage.setLayoutParams(new ViewGroup.LayoutParams(itemSize, itemSize));
                seatImage.setImageResource(R.drawable.seat);
                seatImage.setScaleType(ImageView.ScaleType.FIT_CENTER);

                char columnLetter = (char) ('A' + col);
                String seatLabel = (i + 1) + String.valueOf(columnLetter);

                TextView seatLabelText = new TextView(this);
                seatLabelText.setText(seatLabel);
                seatLabelText.setTextSize(10);
                seatLabelText.setGravity(Gravity.CENTER);
                seatLabelText.setTextColor(Color.BLACK);

                seatContainer.addView(seatImage);
                seatContainer.addView(seatLabelText);
                seatMap.put(seatLabel, seatContainer);

                seatContainer.setOnClickListener(view -> {
                    if ("selected".equals(seatImage.getTag())) {
                        seatImage.setColorFilter(null);
                        seatImage.setTag(null);
                        selectedSeatLabels.remove(seatLabel);
                    } else {
                        seatImage.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
                        seatImage.setTag("selected");
                        selectedSeatLabels.add(seatLabel);
                    }
                    selectedSeats = selectedSeatLabels.size();
                    totalFare = selectedSeats * seatPrice;
                    totalFareText.setText("Total Fare: ₹" + totalFare);
                });

                gridSeats.addView(seatContainer);
                seatNumber++;
            }
        }

        fetchAndDisableBookedSeats(scheduleId, seatMap);
    }

    private void fetchAndDisableBookedSeats(String scheduleId, Map<String, LinearLayout> seatMap) {
        db.collection("OnlineTicketBooking")
                .whereEqualTo("ScheduleId", db.document("Schedule/" + scheduleId))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> allBookedSeats = new ArrayList<>();

                    for (var doc : queryDocumentSnapshots.getDocuments()) {
                        Object seatData = doc.get("Seats_Number");

                        if (seatData instanceof String) {
                            allBookedSeats.add((String) seatData);
                        }
                        // If you plan to support multiple seats per booking later
                        else if (seatData instanceof List) {
                            allBookedSeats.addAll((List<String>) seatData);
                        }
                    }

                    for (String seatId : allBookedSeats) {
                        if (seatMap.containsKey(seatId)) {
                            LinearLayout seatLayout = seatMap.get(seatId);
                            if (seatLayout != null) {
                                ImageView seatImage = (ImageView) seatLayout.getChildAt(0);

                                // Change the seat image to a "disabled" version (e.g., gray seat image)
                                seatImage.setImageResource(R.drawable.seat_disabled); // Use your disabled seat image

                                seatLayout.setEnabled(false);
                            }
                        }
                    }
                });
    }


}
