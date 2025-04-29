package com.example.busmanagermentapplicationfinal.passenger;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.busmanagermentapplicationfinal.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class PassPreview extends AppCompatActivity {

    TextView tvName, tvDob, tvGender;
    TextView tvMobile, tvEmail, tvAddress, tvCity, tvPincode, tvDistrict;
    TextView tvIdNumber, tvInstitution;
    TextView tvPassType, tvSource, tvDestination, tvDuration;
    TextView tvOriginalAmount, tvDiscount, tvFinalAmount;
    Button btnSubmit;

    FirebaseFirestore firestore;
    String temp;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pass_preview);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        firestore = FirebaseFirestore.getInstance();

        String passRequestId = getIntent().getStringExtra("passrequestId");
        if (passRequestId == null) {
            Toast.makeText(this, "Invalid Request ID", Toast.LENGTH_SHORT).show();
            return;
        }
//        temp="aDNvkrBr2Pr1d8ZpLYqW";

        // View bindings
        tvName = findViewById(R.id.tvName);
        tvDob = findViewById(R.id.tvDob);
        tvGender = findViewById(R.id.tvGender);
        tvMobile = findViewById(R.id.tvMobile);
        tvEmail = findViewById(R.id.tvEmail);
        tvAddress = findViewById(R.id.tvAddress);
        tvCity = findViewById(R.id.tvCity);
        tvPincode = findViewById(R.id.tvPincode);
        tvDistrict = findViewById(R.id.tvDistrict);
        tvIdNumber = findViewById(R.id.tvIdNumber);
        tvInstitution = findViewById(R.id.tvInstitution);
        tvPassType = findViewById(R.id.tvPassType);
        tvSource = findViewById(R.id.tvSource);
        tvDestination = findViewById(R.id.tvDestination);
        tvDuration = findViewById(R.id.tvDuration);
        btnSubmit = findViewById(R.id.btnSub);

        // Fare display text views
        tvOriginalAmount = findViewById(R.id.tvOriginalAmount);
        tvDiscount = findViewById(R.id.tvDiscount);
        tvFinalAmount = findViewById(R.id.tvFinalAmount);

        btnSubmit.setOnClickListener(view -> {
            DocumentReference docRef = firestore.collection("PassRequest").document(passRequestId);

            String totalPayableText = tvFinalAmount.getText().toString(); // e.g., "Total Payable: ₹123.45"
            String amountOnly = totalPayableText.replaceAll("[^\\d.]+", ""); // Extract only the number part (e.g., 123.45)

            double totalPayableAmount = 0;
            try {
                totalPayableAmount = Double.parseDouble(amountOnly);
            } catch (NumberFormatException e) {
                Toast.makeText(PassPreview.this, "Invalid total payable amount", Toast.LENGTH_SHORT).show();
                return;
            }

            docRef.update("Status", "Pending", "TotalPayable", totalPayableAmount)
                    .addOnSuccessListener(unused -> {
                        Intent intent = new Intent(PassPreview.this, HomePage.class);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(PassPreview.this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
        });


        fetchPassRequestDetails(passRequestId);
    }

    private void fetchPassRequestDetails(String passRequestId) {
        if (passRequestId == null || passRequestId.isEmpty()) {
            Toast.makeText(this, "Invalid Request ID", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("PassRequest").document(passRequestId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        DocumentReference fromRef = documentSnapshot.getDocumentReference("FromStation");
                        DocumentReference toRef = documentSnapshot.getDocumentReference("ToStation");

                        String fromStation = fromRef.getPath(); // or fromRef.getId()
                        String toStation = toRef.getPath();     // or toRef.getId()
                        String passType = documentSnapshot.getString("Category");
                        String durationText = documentSnapshot.getString("Duration");

                        tvAddress.setText("Address: " + documentSnapshot.getString("Address"));
                        tvCity.setText("City: " + documentSnapshot.getString("City"));
                        tvPincode.setText("Pincode: " + documentSnapshot.getString("Pincode"));
                        tvDistrict.setText("District: " + documentSnapshot.getString("District"));
                        tvIdNumber.setText("ID Number: " + documentSnapshot.getString("IdNumber"));
                        tvInstitution.setText("Institution: " + documentSnapshot.getString("Institution"));
                        tvPassType.setText("Pass Type: " + passType);
                        tvSource.setText("Source: " + fromStation);
                        tvDestination.setText("Destination: " + toStation);
                        tvDuration.setText("Duration: " + durationText);

                        String passengerID = documentSnapshot.getString("passengerId");
                        firestore.collection("Passenger").document(passengerID).get()
                                .addOnSuccessListener(passengerSnapshot -> {
                                    if (passengerSnapshot.exists()) {
                                        tvName.setText("Name: " + passengerSnapshot.getString("Name"));
                                        tvDob.setText("DOB: " + passengerSnapshot.getString("DOB"));
                                        tvGender.setText("Gender: " + passengerSnapshot.getString("Gender"));
                                        tvMobile.setText("Mobile: " + passengerSnapshot.getString("Contact_no"));
                                        tvEmail.setText("Email: " + passengerSnapshot.getString("Email"));
                                    }
                                });

                        // Calculate fare with Firestore logic
                        calculateTotalFare(fromStation, toStation, passType, durationText);

                    } else {
                        Toast.makeText(this, "No data found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to fetch data: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void calculateTotalFare(String fromStationRef, String toStationRef, String passType, String durationText) {
        double farePerKm = 2.5;
        int durationMultiplier;

        switch (durationText) {
            case "Quarterly":
                durationMultiplier = 3;
                break;
            case "Yearly":
                durationMultiplier = 12;
                break;
            default:
                durationMultiplier = 1;
                break;
        }

        // Fetch both stations
        firestore.document(fromStationRef).get().addOnSuccessListener(fromSnapshot -> {
            firestore.document(toStationRef).get().addOnSuccessListener(toSnapshot -> {
                if (fromSnapshot.exists() && toSnapshot.exists()) {
                    com.google.firebase.firestore.GeoPoint fromGeo = fromSnapshot.getGeoPoint("Location");
                    com.google.firebase.firestore.GeoPoint toGeo = toSnapshot.getGeoPoint("Location");

                    if (fromGeo != null && toGeo != null) {
                        double distance = calculateDistance(fromGeo.getLatitude(), fromGeo.getLongitude(), toGeo.getLatitude(), toGeo.getLongitude());

                        String fromStationName = fromSnapshot.getString("Name");
                        String toStationName = toSnapshot.getString("Name");

                        tvSource.setText("Source: " + fromStationName);
                        tvDestination.setText("Destination: " + toStationName);

                        // Fetch discount
                        firestore.collection("Discount")
                                .whereEqualTo("Pass_type", passType)
                                .whereEqualTo("Status", "Active")
                                .get()
                                .addOnSuccessListener(discountSnapshots -> {
                                    double discountPercentage = 0;
                                    if (!discountSnapshots.isEmpty()) {
                                        discountPercentage = discountSnapshots.getDocuments().get(0).getDouble("Percentage");
                                    }

                                    double originalAmount = distance * farePerKm * durationMultiplier;
                                    double discountAmount = originalAmount * (discountPercentage / 100);
                                    double finalAmount = originalAmount - discountAmount;

                                    tvOriginalAmount.setText("Total Amount: ₹" + String.format("%.2f", originalAmount));
                                    tvDiscount.setText("- Discount (" + discountPercentage + "%): ₹" + String.format("%.2f", discountAmount));
                                    tvFinalAmount.setText("Total Payable: ₹" + String.format("%.2f", finalAmount));
                                });
                    }
                } else {
                    Toast.makeText(this, "Station data missing", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c; // distance in kilometers
    }


}
