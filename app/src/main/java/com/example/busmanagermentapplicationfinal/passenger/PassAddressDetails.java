package com.example.busmanagermentapplicationfinal.passenger;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.busmanagermentapplicationfinal.R;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PassAddressDetails extends AppCompatActivity {
    EditText etAddress, etCity, etPincode, etDistrict;
    Button btnNext;

    FirebaseFirestore db;

    String passrequestId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pass_address_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        etAddress = findViewById(R.id.etAddress);
        etCity = findViewById(R.id.etCity);
        etPincode = findViewById(R.id.etPincode);
        etDistrict = findViewById(R.id.etDistrict);
        btnNext = findViewById(R.id.btnNext);

        db = FirebaseFirestore.getInstance();


        CircularProgressIndicator progressCircle = findViewById(R.id.progressCircle);
        TextView tvStepText = findViewById(R.id.tvStepText);

        // Set progress for "step 2 of 4"
        int currentStep = 2;
        int totalSteps = 4;

        int progressPercent = (int) ((currentStep / (float) totalSteps) * 100);
        progressCircle.setProgress(progressPercent);
        tvStepText.setText(currentStep + " of " + totalSteps);


        passrequestId = getIntent().getStringExtra("passrequestId");
        Toast.makeText(this, "Pass Request from address ID: " + passrequestId, Toast.LENGTH_SHORT).show();
        btnNext.setOnClickListener(v -> {
            String address = etAddress.getText().toString().trim();
            String city = etCity.getText().toString().trim();
            String pincode = etPincode.getText().toString().trim();
            String district = etDistrict.getText().toString().trim();

            // Validations
            if (TextUtils.isEmpty(address)) {
                etAddress.setError("Enter address");
                return;
            }

            if (TextUtils.isEmpty(city)) {
                etCity.setError("Enter city/taluka");
                return;
            }

            if (TextUtils.isEmpty(pincode) || pincode.length() != 6) {
                etPincode.setError("Enter valid 6-digit pincode");
                return;
            }

            if (TextUtils.isEmpty(district)) {
                etDistrict.setError("Enter district");
                return;
            }

            // Save to Firestore
            Map<String, Object> addressMap = new HashMap<>();
            addressMap.put("Address", address);
            addressMap.put("City", city);
            addressMap.put("Pincode", pincode);
            addressMap.put("District", district);

            db.collection("PassRequest").document(passrequestId)
                    .update(addressMap)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Address Saved", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(PassAddressDetails.this, PassIDvarifiaction.class); // Replace with next activity
                        intent.putExtra("passrequestId", passrequestId);
                        startActivity(intent);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to save: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }
}