package com.example.busmanagermentapplicationfinal.conductor;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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
import com.google.firebase.firestore.GeoPoint;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Conductor_TicketBooking extends ConductorBaseActivity implements PaymentResultListener {

    AutoCompleteTextView fromStation, toStation;
    EditText etContact, etSeats;
    Spinner paymentModeSpinner;
    Button btnPay;
    FirebaseFirestore db;
    String busId = "xaVVOoGvT0Dl8ls0hVaT";
    int farePerSeat = 100; // fallback if distance fails
    Map<String, DocumentReference> stationMap = new HashMap<>();
    String contactNo, paymentMode;
    DocumentReference fromRef, toRef;
    int totalSeats;
    double calculatedDistance = 0.0;

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

        loadStations();
        loadPaymentModes();

        btnPay.setOnClickListener(view -> {
            if (validateFields()) {
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
                });
    }

    private void loadPaymentModes() {
        String[] modes = {"Cash", "Online"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, modes);
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
        DocumentReference busRef = db.collection("Bus_Details").document(busId);

        busRef.get().addOnSuccessListener(busSnapshot -> {
            if (busSnapshot.exists()) {
                int perKmFare = busSnapshot.getLong("BusFare_Fee").intValue();
                int totalAmount = (int) (perKmFare * calculatedDistance * totalSeats * 100); // in paisa

                Checkout checkout = new Checkout();
                checkout.setKeyID("rzp_test_Gu3YzpwsCudp5k");

                JSONObject options = new JSONObject();
                try {
                    options.put("name", "Bus Ticket");
                    options.put("description", "Offline Booking");
                    options.put("currency", "INR");
                    options.put("amount", totalAmount);

                    JSONObject prefill = new JSONObject();
                    prefill.put("contact", contactNo);
                    prefill.put("email", "test@example.com");

                    options.put("prefill", prefill);
                    checkout.open(Conductor_TicketBooking.this, options);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        DocumentReference busRef = db.collection("Bus_Details").document(busId);

        busRef.get().addOnSuccessListener(busSnapshot -> {
            if (busSnapshot.exists()) {
                int perKmFare = busSnapshot.getLong("BusFare_Fee").intValue();
                int totalAmount = (int) (perKmFare * calculatedDistance * totalSeats);

                Map<String, Object> booking = new HashMap<>();
                booking.put("Bus_id", busRef);
                booking.put("From", fromRef);
                booking.put("To", toRef);
                booking.put("ContactNo", contactNo);
                booking.put("TotalSeats", totalSeats);

                db.collection("OfflineTicketBooking").add(booking)
                        .addOnSuccessListener(bookingRef -> {
                            Map<String, Object> transaction = new HashMap<>();
                            transaction.put("Booking_id", bookingRef);
                            transaction.put("Payment_mode", "NetBanking");
                            transaction.put("Total_amount", totalAmount);

                            db.collection("Transaction").add(transaction)
                                    .addOnSuccessListener(t -> {
                                        Toast.makeText(this, "Booking & Payment Saved!", Toast.LENGTH_SHORT).show();
                                        resetForm();
                                    });
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(this, "Failed to save booking", Toast.LENGTH_SHORT).show();
                        });

            } else {
                Toast.makeText(this, "Bus fare not found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPaymentError(int i, String s) {
        Toast.makeText(this, "Payment Failed: " + s, Toast.LENGTH_LONG).show();
    }

    private void saveTransaction(String mode, String transactionId) {
        DocumentReference busRef = db.collection("Bus_Details").document(busId);

        busRef.get().addOnSuccessListener(busSnapshot -> {
            if (busSnapshot.exists()) {
                int perKmFare = busSnapshot.getLong("BusFare_Fee").intValue();
                int totalAmount = (int) (perKmFare * calculatedDistance * totalSeats);

                // Step 1: Create Transaction record
                Map<String, Object> transaction = new HashMap<>();
                transaction.put("Payment_mode", mode);
                transaction.put("TransactionId", transactionId);
                transaction.put("Total_amount", totalAmount);

                db.collection("Transaction").add(transaction)
                        .addOnSuccessListener(transactionRef -> {
                            // Step 2: Create Booking record with Transaction reference
                            Map<String, Object> booking = new HashMap<>();
                            booking.put("Bus_id", busRef);
                            booking.put("From", fromRef);
                            booking.put("To", toRef);
                            booking.put("ContactNo", contactNo);
                            booking.put("TotalSeats", totalSeats);
                            booking.put("Transaction_ref", transactionRef); // Reference to Transaction

                            db.collection("OfflineTicketBooking").add(booking)
                                    .addOnSuccessListener(documentReference -> {
                                        Toast.makeText(this, mode.equals("Cash") ? "Ticket Booked (Cash)" : "Booking Successful", Toast.LENGTH_SHORT).show();
                                        resetForm();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(this, "Failed to save booking", Toast.LENGTH_SHORT).show());
                        })
                        .addOnFailureListener(e -> Toast.makeText(this, "Failed to save transaction", Toast.LENGTH_SHORT).show());
            }
        }).addOnFailureListener(e -> Toast.makeText(this, "Failed to fetch bus details", Toast.LENGTH_SHORT).show());
    }


    private void resetForm() {
        fromStation.setText("");
        toStation.setText("");
        etContact.setText("");
        etSeats.setText("");
        paymentModeSpinner.setSelection(0);
    }

    public static double getDistanceInKm(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of Earth in kilometers
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
