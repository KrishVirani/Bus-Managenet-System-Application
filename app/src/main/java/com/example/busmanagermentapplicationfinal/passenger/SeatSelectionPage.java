package com.example.busmanagermentapplicationfinal.passenger;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.busmanagermentapplicationfinal.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class SeatSelectionPage extends AppCompatActivity implements PaymentResultListener {

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
    String busId, busName, busType, busCategory, plateNumber, layout, fromStationId, toStationId, selectedDate, passengerId = "7NTNWfsOaHUnHNdagUTg";

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
        btnBack2 = findViewById(R.id.btnback2);
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

            String fromStationName = extras.getStringExtra("from");
            String toStationName = extras.getStringExtra("to");

// Fetch station document IDs
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
            try {
                if (selectedSeats == 0) {
                    Toast.makeText(this, "Please select seats", Toast.LENGTH_SHORT).show();
                    return;
                }
                startPayment();
            } catch (JSONException e) {
                throw new RuntimeException(e);
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
                    } else {
                        Toast.makeText(this, "Station not found: " + stationName, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch station ID", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchSeatLayoutFromFirebase() {
        db.collection("Bus_Details").document(busId).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                capacity = documentSnapshot.getLong("Capacity").intValue();
                seatPrice = documentSnapshot.getLong("BusFare_Fee").intValue();
                layout = documentSnapshot.getString("Dimension");

                if (layout != null && layout.contains("x")) {
                    String[] parts = layout.split("x");
                    left = Integer.parseInt(parts[0]);
                    right = Integer.parseInt(parts[1]);
                }

                int columns = left + 1 + right + 2;
                gridSeats.setColumnCount(columns);
                renderSeats(left, right, columns);
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Failed to load seat layout", Toast.LENGTH_SHORT).show();
        });
    }

    private void renderSeats(int left, int right, int totalColumns) {
        int totalSeats = capacity;
        int seatsPerRow = left + right;
        int rows = (int) Math.ceil((double) totalSeats / seatsPerRow);
        int seatNumber = 1;

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int itemSize = screenWidth / (left + right + 4);

        for (int i = 0; i < rows; i++) {
            for (int col = 0; col < totalColumns; col++) {
                if (col == left || col == left + 1 + right) {
                    View gap = new View(this);
                    gap.setLayoutParams(new ViewGroup.LayoutParams(16, itemSize));
                    gridSeats.addView(gap);
                    continue;
                }

                if (col == left + 1) {
                    View aisle = new View(this);
                    aisle.setLayoutParams(new ViewGroup.LayoutParams(32, itemSize));
                    gridSeats.addView(aisle);
                    continue;
                }

                if (seatNumber > totalSeats) break;

                LinearLayout seatContainer = new LinearLayout(this);
                seatContainer.setOrientation(LinearLayout.VERTICAL);
                seatContainer.setGravity(Gravity.CENTER);
                seatContainer.setPadding(4, 4, 4, 4);

                ImageView seatImage = new ImageView(this);
                seatImage.setLayoutParams(new ViewGroup.LayoutParams(itemSize, itemSize));
                seatImage.setImageResource(R.drawable.seat);
                seatImage.setScaleType(ImageView.ScaleType.FIT_CENTER);

                TextView priceText = new TextView(this);
                priceText.setText("₹" + seatPrice);
                priceText.setTextSize(10);
                priceText.setGravity(Gravity.CENTER);
                priceText.setTextColor(Color.BLACK);

                seatContainer.addView(seatImage);
                seatContainer.addView(priceText);

                seatContainer.setOnClickListener(view -> {
                    if ("selected".equals(seatImage.getTag())) {
                        seatImage.setColorFilter(null);
                        seatImage.setTag(null);
                        selectedSeats--;
                    } else {
                        seatImage.setColorFilter(Color.GREEN, PorterDuff.Mode.SRC_IN);
                        seatImage.setTag("selected");
                        selectedSeats++;
                    }
                    totalFare = selectedSeats * seatPrice;
                    totalFareText.setText("Total Fare: ₹" + totalFare);
                });

                gridSeats.addView(seatContainer);
                seatNumber++;
            }
        }
    }

    private void startPayment() throws JSONException {
        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_Gu3YzpwsCudp5k");

        JSONObject options = new JSONObject();
        options.put("name", "Bus Booking System");
        options.put("description", "ticket online booking");
        options.put("currency", "INR");
        options.put("amount", totalFare * 100); // in paise

        JSONObject prefill = new JSONObject();
        prefill.put("email", "test@example.com");
        prefill.put("contact", "8200731347");
        options.put("prefill", prefill);

        checkout.open(this, options);
    }

    @Override
    public void onPaymentSuccess(String paymentId) {
        Toast.makeText(this, "Payment Successful: " + paymentId, Toast.LENGTH_LONG).show();

        // Save to OnlineTicketBooking
        Map<String, Object> booking = new HashMap<>();
        booking.put("Bus_id", db.document("Bus_Details/" + busId));
        booking.put("Date_of_booking", new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));
        booking.put("From", db.document("Station/" + fromStationId));
        booking.put("To", db.document("Station/" + toStationId));
        booking.put("PassangerId", db.document("Passenger/" + passengerId));
        booking.put("Seats_Number", selectedSeats);

        db.collection("OnlineTicketBooking")
                .add(booking)
                .addOnSuccessListener(bookingRef -> {
                    // Save to Transaction
                    Map<String, Object> transaction = new HashMap<>();
                    transaction.put("OnlineBooking_id", bookingRef);
                    transaction.put("Payment_mode", "NetBanking");
                    transaction.put("Total_amount", totalFare);

                    db.collection("Transaction")
                            .add(transaction)
                            .addOnSuccessListener(transRef -> {
                                Toast.makeText(this, "Booking Confirmed!", Toast.LENGTH_SHORT).show();
                                Intent intent=new Intent(SeatSelectionPage.this,HomePage.class);
                                startActivity(intent);
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(this, "Transaction failed to save", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Booking failed to save", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onPaymentError(int code, String response) {
        Toast.makeText(this, "Payment Failed: " + response, Toast.LENGTH_LONG).show();
    }
}
