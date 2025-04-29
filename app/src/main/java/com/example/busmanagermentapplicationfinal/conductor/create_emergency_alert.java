package com.example.busmanagermentapplicationfinal.conductor;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.busmanagermentapplicationfinal.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import android.location.Location;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class create_emergency_alert extends ConductorBaseActivity {

    EditText etAlertMsg;
    Spinner  sp_bus;
    Button btn_submit_alert;

    FirebaseFirestore firestore;

    ArrayList<String> userNames = new ArrayList<>();
    ArrayList<String> userIds = new ArrayList<>();

    ArrayList<String> busNames = new ArrayList<>();
    ArrayList<String> busIds = new ArrayList<>();

    ImageView bckbtn;
    String selectedUserId = "";
    String selectedBusId = "";
    FusedLocationProviderClient fusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_emergency_alert);
        setupDrawer(R.layout.activity_create_emergency_alert);
        toolbarTitle.setText("Create Emergency Alert");
//        bckbtn = findViewById(R.id.backButton12);
//        bckbtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                finish();
//            }
//        });


        etAlertMsg = findViewById(R.id.et_alert_msg);
        sp_bus = findViewById(R.id.sp_bus);
        btn_submit_alert = findViewById(R.id.btn_submit_alert);

        firestore = FirebaseFirestore.getInstance();
        selectedUserId="00zkLm88jr9mlOrot77Q";
        loadBusesFromFirestore();

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        btn_submit_alert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String alertMsg = etAlertMsg.getText().toString().trim();

                if (alertMsg.isEmpty()) {
                    Toast.makeText(create_emergency_alert.this, "Enter alert message", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selectedUserId.isEmpty() || selectedBusId.isEmpty()) {
                    Toast.makeText(create_emergency_alert.this, "Select user and bus", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(location -> {
                            if (location != null) {
                                GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

                                DocumentReference userRef = firestore.collection("User").document(selectedUserId);
                                DocumentReference busRef = firestore.collection("Bus_Details").document(selectedBusId);

                                Map<String, Object> data = new HashMap<>();
                                data.put("Alert_message", alertMsg);
                                data.put("User_id", userRef);
                                data.put("Bus_id", busRef);
                                data.put("TimeDate", Timestamp.now());
                                data.put("Location", geoPoint); // ✅ Add the location

                                firestore.collection("EmergencyAlert")
                                        .add(data)
                                        .addOnSuccessListener(documentReference -> {
                                            Toast.makeText(create_emergency_alert.this, "Alert sent successfully", Toast.LENGTH_SHORT).show();
                                            etAlertMsg.setText("");
                                            startActivity(new Intent(create_emergency_alert.this, emergency_list.class));
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(create_emergency_alert.this, "Failed to send alert", Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                Toast.makeText(create_emergency_alert.this, "Could not fetch location", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(create_emergency_alert.this, "Location error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
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
