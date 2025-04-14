package com.example.busmanagermentapplicationfinal.driver;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.busmanagermentapplicationfinal.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class driver_create_emergency_alert extends AppCompatActivity {

    EditText etAlertMsg;
    Spinner sp_user, sp_bus;
    Button btn_submit_alert;

    FirebaseFirestore firestore;

    ArrayList<String> userNames = new ArrayList<>();
    ArrayList<String> userIds = new ArrayList<>();

    ArrayList<String> busNames = new ArrayList<>();
    ArrayList<String> busIds = new ArrayList<>();

    String selectedUserId = "";
    String selectedBusId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_emergency_alert);

        etAlertMsg = findViewById(R.id.et_alert_msg);
        sp_user = findViewById(R.id.sp_user);
        sp_bus = findViewById(R.id.sp_bus);
        btn_submit_alert = findViewById(R.id.btn_submit_alert);

        firestore = FirebaseFirestore.getInstance();

        loadUsersFromFirestore();
        loadBusesFromFirestore();

        btn_submit_alert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String alertMsg = etAlertMsg.getText().toString().trim();

                if (alertMsg.isEmpty()) {
                    Toast.makeText(driver_create_emergency_alert.this, "Enter alert message", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selectedUserId.isEmpty() || selectedBusId.isEmpty()) {
                    Toast.makeText(driver_create_emergency_alert.this, "Select user and bus", Toast.LENGTH_SHORT).show();
                    return;
                }

                DocumentReference userRef = firestore.collection("User").document(selectedUserId);
                DocumentReference busRef = firestore.collection("Bus_Details").document(selectedBusId);

                Map<String, Object> data = new HashMap<>();
                data.put("Alert_message", alertMsg);
                data.put("User_id", userRef);
                data.put("Bus_id", busRef);
                data.put("TimeDate", Timestamp.now());

                firestore.collection("EmergencyAlert")
                        .add(data)
                        .addOnSuccessListener(documentReference -> {
                            Toast.makeText(driver_create_emergency_alert.this, "Alert sent successfully", Toast.LENGTH_SHORT).show();
                            etAlertMsg.setText("");

                            // Redirect to Emergency List Page
                            startActivity(new Intent(driver_create_emergency_alert.this, emergency_list.class));
                            finish(); // Optional: remove current activity from backstack
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(driver_create_emergency_alert.this, "Failed to send alert", Toast.LENGTH_SHORT).show();
                        });
            }
        });

    }

    private void loadUsersFromFirestore() {
        firestore.collection("User")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    userNames.clear();
                    userIds.clear();

                    for (DocumentSnapshot doc : querySnapshot) {
                        String id = doc.getId();
                        String name = doc.getString("FirstName");

                        if (name != null) {
                            userNames.add(name);
                            userIds.add(id);
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, userNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_user.setAdapter(adapter);

                    sp_user.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedUserId = userIds.get(position);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            selectedUserId = "";
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load users", Toast.LENGTH_SHORT).show();
                });
    }

    private void loadBusesFromFirestore() {
        firestore.collection("Bus_Details")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    busNames.clear();
                    busIds.clear();

                    for (DocumentSnapshot doc : querySnapshot) {
                        String id = doc.getId();
                        String name = doc.getString("BusName");

                        if (name != null) {
                            busNames.add(name);
                            busIds.add(id);
                        }
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, busNames);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    sp_bus.setAdapter(adapter);

                    sp_bus.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            selectedBusId = busIds.get(position);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            selectedBusId = "";
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load buses", Toast.LENGTH_SHORT).show();
                });
    }
}
