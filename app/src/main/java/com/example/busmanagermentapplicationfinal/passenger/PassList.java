package com.example.busmanagermentapplicationfinal.passenger;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.busmanagermentapplicationfinal.R;
import com.example.busmanagermentapplicationfinal.conductor.Conductor_TicketBooking;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PassList extends PassengerBaseActivity implements PaymentResultListener {
    Button btnNewPass;
    LinearLayout passContainer;
    FirebaseFirestore firestore;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pass_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setContentLayout(R.layout.activity_pass_list);
        toolbarTitle.setText("Pass Requests");
        btnNewPass = findViewById(R.id.btnNewPass);
        btnNewPass.setOnClickListener(v -> {
            Intent intent = new Intent(PassList.this, PassPersonalDetails.class);
            startActivity(intent);
        });
        passContainer = findViewById(R.id.passContainer);
        firestore = FirebaseFirestore.getInstance();

        loadPassRequests();
    }

    private void loadPassRequests() {
        firestore.collection("PassRequest")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        DocumentReference fromRef = doc.getDocumentReference("FromStation");
                        DocumentReference toRef = doc.getDocumentReference("ToStation");
                        Double payable = doc.getDouble("TotalPayable");
                        String status = doc.getString("Status");

                        if (fromRef != null && toRef != null && payable != null && status != null) {
                            String fromId = fromRef.getId();
                            String toId = toRef.getId();

                            // Fetch both station names
                            fetchStationName(fromId, fromName -> {
                                fetchStationName(toId, toName -> {
                                    addPassView(fromName, toName, payable, status);
                                });
                            });
                        }
                    }
                });

    }
    private void fetchStationName(String stationId, OnStationNameFetched callback) {
        firestore.collection("Station").document(stationId)
                .get()
                .addOnSuccessListener(doc -> {
                    String name = doc.getString("Name");
                    if (name != null) {
                        callback.onFetched(name);
                    } else {
                        callback.onFetched("Unknown");
                    }
                })
                .addOnFailureListener(e -> {
                    callback.onFetched("Error");
                });
    }

    interface OnStationNameFetched {
        void onFetched(String name);
    }
    private void addPassView(String from, String to, double payable, String status) {
        // Card-like layout
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(24, 24, 24, 24);
        card.setBackgroundResource(R.drawable.white_rounded_card);

        // Source-Destination
        TextView sourceDest = new TextView(this);
        sourceDest.setText("From: " + from + " → To: " + to);
        sourceDest.setTextSize(16f);
        sourceDest.setTypeface(null, Typeface.BOLD);

        // Payable
        TextView payableText = new TextView(this);
        payableText.setText("Payable Amount: ₹" + String.format("%.2f", payable));
        payableText.setTextSize(15f);

        // Status
        TextView statusText = new TextView(this);
        statusText.setText("Status: " + status);
        statusText.setTextSize(15f);

        // Pay Button
        Button btnPay = new Button(this);
        btnPay.setText("Pay");
        btnPay.setVisibility(status.equalsIgnoreCase("Approved") ? View.VISIBLE : View.GONE);

        // Pay click logic
        btnPay.setOnClickListener(v -> {
            Toast.makeText(this, "Payment started for " + from + " → " + to, Toast.LENGTH_SHORT).show();
            // TODO: Start Payment Activity
            try {
                startPayment((int) payable);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });

        // Add views to card
        card.addView(sourceDest);
        card.addView(payableText);
        card.addView(statusText);
        card.addView(btnPay);

        // Margin between cards
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 32);
        card.setLayoutParams(params);

        passContainer.addView(card);
    }

    private void startPayment(int totalAmount) throws JSONException {
        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_Gu3YzpwsCudp5k");

        JSONObject options = new JSONObject();
        try {
            options.put("name", "Bus Ticket");
            options.put("description", "Pass Payment");
            options.put("currency", "INR");
            options.put("amount", totalAmount);

            JSONObject prefill = new JSONObject();
            prefill.put("contact", "9099331755");
            prefill.put("email", "test@example.com");

            options.put("prefill", prefill);
            checkout.open(PassList.this, options);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        //update status to Active
        Map<String, Object> update = new HashMap<>();
        update.put("Status", "Active");
        firestore.collection("PassRequest").document(razorpayPaymentID).update(update);
        Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(PassList.this, HomePage.class);
        startActivity(intent);
    }

    @Override
    public void onPaymentError(int i, String s) {
        Toast.makeText(this, "Payment Failed: " + s, Toast.LENGTH_LONG).show();
    }
}