package com.example.busmanagermentapplicationfinal.driver;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.busmanagermentapplicationfinal.R;
import com.example.busmanagermentapplicationfinal.conductor.ConductorBaseActivity;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class emergency_list extends ConductorBaseActivity {

    Button btn_create_alert;
    TextView tv_alert_result;
    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_list);

        btn_create_alert = findViewById(R.id.btn_create_alert);
        tv_alert_result = findViewById(R.id.tv_alert_result);
        firestore = FirebaseFirestore.getInstance();

        btn_create_alert.setOnClickListener(v -> {
            Intent intent = new Intent(emergency_list.this, driver_create_emergency_alert.class);
            startActivity(intent);
        });

        loadEmergencyAlertsFromFirestore();
    }

    private void loadEmergencyAlertsFromFirestore() {
        firestore.collection("EmergencyAlert")
                .orderBy("TimeDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        tv_alert_result.setText("No emergency alerts yet");
                        return;
                    }

                    StringBuilder builder = new StringBuilder();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String message = doc.getString("Alert_message");
                        Timestamp timestamp = doc.getTimestamp("TimeDate");
                        Date date = timestamp != null ? timestamp.toDate() : null;
                        String timeStr = (date != null)
                                ? new SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault()).format(date)
                                : "N/A";

                        DocumentReference userRef = doc.getDocumentReference("User_id");
                        DocumentReference busRef = doc.getDocumentReference("Bus_id");

                        // Use array for mutable values inside nested callbacks
                        String[] tempResult = {""};

                        userRef.get().addOnSuccessListener(userDoc -> {
                            String userName = userDoc.getString("FirstName");

                            busRef.get().addOnSuccessListener(busDoc -> {
                                String busName = busDoc.getString("BusName");

                                builder.append("Message: ").append(message).append("\n")
                                        .append("DateTime: ").append(timeStr).append("\n")
                                        .append("Bus: ").append(busName != null ? busName : "N/A").append("\n\n");

                                // Set text only after last item
                                if (querySnapshot.getDocuments().indexOf(doc) == querySnapshot.size() - 1) {
                                    tv_alert_result.setText(builder.toString());
                                }
                            });
                        });
                    }

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load data.", Toast.LENGTH_SHORT).show();
                });
    }
}
