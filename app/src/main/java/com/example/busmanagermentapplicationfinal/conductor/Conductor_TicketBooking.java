package com.example.busmanagermentapplicationfinal.conductor;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.busmanagermentapplicationfinal.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

public class Conductor_TicketBooking extends ConductorBaseActivity implements PaymentResultListener {

    AutoCompleteTextView fromStation, toStation;
    EditText etContact, etSeats;
    TextView tvDisplayTotalAmount ;
    TextView tvShiftDate, tvDestinationStation; // NEW: Display shift date and destination
    Spinner paymentModeSpinner;
    Button btnPay;
    FirebaseFirestore db;
    String conductorId = "syG2YRgAF8h6dsCh2gRo"; // You can fetch this dynamically based on login
    DocumentReference busRef; // Dynamic bus reference
    int farePerSeat = 100;
    Map<String, DocumentReference> stationMap = new HashMap<>();
    String contactNo, paymentMode;
    DocumentReference fromRef, toRef;
    int totalSeats;
    double calculatedDistance = 0.0;
    ImageView backButton;
    LinearLayout container1;
    DocumentReference scheduleRef;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_conductor_ticket_booking);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        backButton = findViewById(R.id.backButton1);

        backButton.setOnClickListener(v -> finish());
        setupDrawer(R.layout.activity_conductor_ticket_booking);
        toolbarTitle.setText("Ticket Booking");

        db = FirebaseFirestore.getInstance();
        Checkout.preload(this);

        fromStation = findViewById(R.id.fromStation);
        toStation = findViewById(R.id.toStation);
        etContact = findViewById(R.id.etContact);
        etSeats = findViewById(R.id.etSeats);
        paymentModeSpinner = findViewById(R.id.spinnerPaymentMode);
        btnPay = findViewById(R.id.btnpay1);
        tvDisplayTotalAmount = findViewById(R.id.tvDisplayAmount);
        tvShiftDate = findViewById(R.id.tvShiftDate); // NEW
        tvDestinationStation = findViewById(R.id.tvDestinationStation); // NEW

        container1 = findViewById(R.id.container1);
        btnPay.setEnabled(false); // Disable until busId is fetched
        loadStations();
        loadPaymentModes();

        fetchBusIdForToday(() -> btnPay.setEnabled(true)); // Fetch busId and enable button

        btnPay.setOnClickListener(view -> {
            if (validateFields()) {
                String destination = toStation.getText().toString().trim();
                tvDestinationStation.setText("To Station: " + destination); // NEW

                fetchStationGeoPointsAndCalculate(() -> {
                    if (paymentMode.equals("Cash")) {
                        saveTransaction("Cash", "");
                    } else {
                        try {
                            startPayment();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void fetchBusIdForToday(Runnable onBusIdFetched) {
        String today = new SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(new Date());

        db.collection("Schedule")
                .whereEqualTo("conductoreId", db.document("User/" + conductorId))
                .whereEqualTo("ShiftDate", today)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!snapshots.isEmpty()) {
                        DocumentSnapshot schedule = snapshots.getDocuments().get(0);
                        scheduleRef = schedule.getReference();

                        busRef = schedule.getDocumentReference("busId");
                        DocumentReference routeRef = schedule.getDocumentReference("routeId");

                        String shiftDate = schedule.getString("ShiftDate");
                        tvShiftDate.setText("Shift Date: " + shiftDate);

                        // Fetch Bus Name
                        busRef.get().addOnSuccessListener(busSnap -> {
                            if (busSnap.exists()) {
                                String busName = busSnap.getString("BusName");
                                TextView tvBusName = findViewById(R.id.tvBusName);
                                tvBusName.setText("Bus Name: " + busName);
                            }
                        });

                        // Fetch Source and Destination Station Names from Route
                        routeRef.get().addOnSuccessListener(routeSnap -> {
                            if (routeSnap.exists()) {
                                DocumentReference sourceRef = routeSnap.getDocumentReference("Source");
                                DocumentReference destinationRef = routeSnap.getDocumentReference("Destination");

                                sourceRef.get().addOnSuccessListener(sourceSnap -> {
                                    if (sourceSnap.exists()) {
                                        String sourceName = sourceSnap.getString("Name");
                                        TextView tvSource = findViewById(R.id.tvSourceStation);
                                        tvSource.setText("Source : " + sourceName);
                                    }
                                });

                                destinationRef.get().addOnSuccessListener(destSnap -> {
                                    if (destSnap.exists()) {
                                        String destName = destSnap.getString("Name");
                                        TextView tvDest = findViewById(R.id.tvDestinationStation);
                                        tvDest.setText("Destination : " + destName);
                                    }
                                });
                            }
                        });

                        onBusIdFetched.run();
                    } else {
                        // Clear the existing views (the form) and show only no_schedule_box layout below header
                        container1.removeAllViews();
                        View noScheduleView = getLayoutInflater().inflate(R.layout.no_schedule_box, container1, false);
                        container1.addView(noScheduleView);
                    }
                })
                .addOnFailureListener(e -> showErrorLayout(R.layout.no_schedule_error));
    }

    private void showErrorLayout(int layoutResId) {
        FrameLayout container = findViewById(R.id.scheduleContainer);
        container.removeAllViews();
        View errorView = getLayoutInflater().inflate(layoutResId, container, false);
        container.addView(errorView);
    }

    private void loadStations() {
        db.collection("Station")
                .whereEqualTo("Status", "Active")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> stationNames = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("Name");
                        stationNames.add(name);
                        stationMap.put(name, doc.getReference());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, stationNames);
                    fromStation.setAdapter(adapter);
                    toStation.setAdapter(adapter);
                    fromStation.setThreshold(1);
                    toStation.setThreshold(1);
                });
    }

    private void loadPaymentModes() {
        List<String> modes = Arrays.asList("Select Payment Mode", "Cash", "Online");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, modes) {
            @Override
            public boolean isEnabled(int position) {
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(position == 0 ? Color.GRAY : Color.BLACK);
                return view;
            }
        };

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        paymentModeSpinner.setAdapter(adapter);
    }

    private boolean validateFields() {
        String from = fromStation.getText().toString().trim();
        String to = toStation.getText().toString().trim();
        contactNo = etContact.getText().toString().trim();
        String seatsStr = etSeats.getText().toString().trim();
        paymentMode = paymentModeSpinner.getSelectedItem().toString();

        if (!stationMap.containsKey(from) || !stationMap.containsKey(to)) {
            Toast.makeText(this, "Invalid station selection", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (contactNo.length() != 10) {
            Toast.makeText(this, "Invalid contact number", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (seatsStr.isEmpty()) {
            Toast.makeText(this, "Enter seat count", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (paymentMode.equals("Select Payment Mode")) {
            Toast.makeText(this, "Please select a valid payment mode", Toast.LENGTH_SHORT).show();
            return false;
        }

        totalSeats = Integer.parseInt(seatsStr);
        fromRef = stationMap.get(from);
        toRef = stationMap.get(to);

        return true;
    }

    private void fetchStationGeoPointsAndCalculate(Runnable onComplete) {
        fromRef.get().addOnSuccessListener(fromSnap -> {
            toRef.get().addOnSuccessListener(toSnap -> {
                if (fromSnap.contains("Location") && toSnap.contains("Location")) {
                    GeoPoint fromGeo = fromSnap.getGeoPoint("Location");
                    GeoPoint toGeo = toSnap.getGeoPoint("Location");
                    calculatedDistance = getDistanceInKm(
                            fromGeo.getLatitude(), fromGeo.getLongitude(),
                            toGeo.getLatitude(), toGeo.getLongitude()
                    );
                }
                onComplete.run();
            });
        });
    }

    private void startPayment() throws JSONException {
        busRef.get().addOnSuccessListener(busSnapshot -> {
            if (busSnapshot.exists()) {
                int perKmFare = Integer.parseInt(busSnapshot.getString("BusFare_Fee"));
                int totalAmount = (int) (perKmFare * calculatedDistance * totalSeats);

                Checkout checkout = new Checkout();
                checkout.setKeyID("rzp_test_QGtm2IFazADN22");

                JSONObject options = new JSONObject();
                try {
                    options.put("name", "Bus Ticket");
                    options.put("description", "Offline Booking");
                    options.put("currency", "INR");
                    options.put("amount", totalAmount * 100); // Amount in paise

                    JSONObject prefill = new JSONObject();
                    prefill.put("contact", contactNo);
                    prefill.put("email", "test@example.com");

                    options.put("prefill", prefill);
                    checkout.open(Conductor_TicketBooking.this, options);
                } catch (JSONException e) {
                    showErrorLayout(R.layout.no_schedule_error);
                }
            }
        });
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        busRef.get().addOnSuccessListener(busSnapshot -> {
            if (busSnapshot.exists()) {
                int perKmFare = Integer.parseInt(busSnapshot.getString("BusFare_Fee"));
                int totalAmount = (int) (perKmFare * calculatedDistance * totalSeats);
                tvDisplayTotalAmount.setText("Total Amount: ₹" + totalAmount);

                Map<String, Object> booking = new HashMap<>();
                booking.put("Bus_id", busRef);
                booking.put("From", fromRef);
                booking.put("To", toRef);
                booking.put("ContactNo", contactNo);
                booking.put("TotalSeats", totalSeats);
                booking.put("Schedule_id", scheduleRef);
                booking.put("TotalAmount", totalAmount);


                db.collection("OfflineTicketBooking").add(booking)
                        .addOnSuccessListener(bookingRef -> {
                            Map<String, Object> transaction = new HashMap<>();
                            transaction.put("Booking_id", bookingRef);
                            transaction.put("Payment_mode", "NetBanking");
                            transaction.put("Total_amount", totalAmount);
                            booking.put("Schedule_id", scheduleRef);

                            db.collection("Transaction").add(transaction)
                                    .addOnSuccessListener(t -> {
                                        Toast.makeText(this, "Booking & Payment Saved!", Toast.LENGTH_SHORT).show();
                                        resetForm();
                                    });
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to save booking", Toast.LENGTH_SHORT).show());
            }
        });
    }

    @Override
    public void onPaymentError(int i, String s) {
        Toast.makeText(this, "Payment Failed: " + s, Toast.LENGTH_LONG).show();
    }

    private void saveTransaction(String mode, String transactionId) {
        busRef.get().addOnSuccessListener(busSnapshot -> {
            if (busSnapshot.exists()) {
                int perKmFare = Integer.parseInt(busSnapshot.getString("BusFare_Fee"));
                int totalAmount = (int) (perKmFare * calculatedDistance * totalSeats);
                tvDisplayTotalAmount.setText("Total Amount: ₹" + totalAmount);

                // Create booking object with required details
                Map<String, Object> booking = new HashMap<>();
                booking.put("Bus_id", busRef);
                booking.put("From", fromRef);
                booking.put("To", toRef);
                booking.put("ContactNo", contactNo);
                booking.put("TotalSeats", totalSeats);
                booking.put("Schedule_id", scheduleRef);
                booking.put("TotalAmount", totalAmount);


                // Add the booking data to the "OfflineTicketBooking" collection
                db.collection("OfflineTicketBooking").add(booking)
                        .addOnSuccessListener(bookingRef -> {
                            // Booking successfully added, now create a transaction record
                            Map<String, Object> transaction = new HashMap<>();
                            transaction.put("Booking_id", bookingRef.getId());  // Use the generated booking reference ID
                            transaction.put("Payment_mode", mode);
                            transaction.put("Total_amount", totalAmount);

                            // Add the transaction record to the "Transaction" collection
                            db.collection("Transaction").add(transaction)
                                    .addOnSuccessListener(transactionRef -> {
                                        // Payment and booking saved successfully
                                        Toast.makeText(getApplicationContext(), "Payment Success", Toast.LENGTH_SHORT).show();
                                        resetForm();  // Reset the form after successful payment and transaction
                                    })
                                    .addOnFailureListener(e -> {
                                        // Handle failure to store transaction
                                        Toast.makeText(getApplicationContext(), "Failed to save transaction", Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .addOnFailureListener(e -> {
                            // Handle failure to store booking
                            Toast.makeText(getApplicationContext(), "Failed to save booking", Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }


    private void resetForm() {
        fromStation.setText("");
        toStation.setText("");
        etContact.setText("");
        etSeats.setText("");
        paymentModeSpinner.setSelection(0);
        tvDisplayTotalAmount.setText("Total Amount: ₹0");
    }

    private double getDistanceInKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Radius of Earth in km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // Distance in km
    }
}
