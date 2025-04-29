package com.example.busmanagermentapplicationfinal.passenger;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.busmanagermentapplicationfinal.R;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class PassPersonalDetails extends AppCompatActivity {
    EditText etName, etDob, etMobile, etEmail;
    RadioGroup radioGender;
    Button btnNext;
    FirebaseFirestore db;
    String passrequestId;
    String PassngerID="BtO4LN6v59lXeCM2eYja";
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pass_personal_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        etName = findViewById(R.id.etName);
        etDob = findViewById(R.id.etDob);
        etMobile = findViewById(R.id.etMobile);
        etEmail = findViewById(R.id.etEmail);
        radioGender = findViewById(R.id.radioGender);
        btnNext = findViewById(R.id.btnNext);

        db = FirebaseFirestore.getInstance();

        autofillDetails();
        CircularProgressIndicator progressCircle = findViewById(R.id.progressCircle);
        TextView tvStepText = findViewById(R.id.tvStepText);

        // Set progress for "step 2 of 4"
        int currentStep = 1;
        int totalSteps = 4;

        int progressPercent = (int) ((currentStep / (float) totalSteps) * 100);
        progressCircle.setProgress(progressPercent);
        tvStepText.setText(currentStep + " of " + totalSteps);

        btnNext.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String dob = etDob.getText().toString().trim();
            String mobile = etMobile.getText().toString().trim();
            String email = etEmail.getText().toString().trim();

            int genderId = radioGender.getCheckedRadioButtonId();
            RadioButton selectedGenderBtn = findViewById(genderId);
            String gender = selectedGenderBtn != null ? selectedGenderBtn.getText().toString() : "";

            // Validation
            if (TextUtils.isEmpty(name)) {
                etName.setError("Enter full name");
                return;
            }

            if (TextUtils.isEmpty(gender)) {
                Toast.makeText(this, "Select gender", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(dob)) {
                etDob.setError("Enter date of birth");
                return;
            }

            if (TextUtils.isEmpty(mobile) || mobile.length() != 10) {
                etMobile.setError("Enter valid 10-digit mobile number");
                return;
            }

            if (!TextUtils.isEmpty(email) && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.setError("Enter valid email address");
                return;
            }

            Map<String, Object> data = new HashMap<>();
            data.put("Name", name);
            data.put("Gender", gender);
            data.put("DOB", dob);
            data.put("Contact_no", mobile);
            data.put("Email", email);

            db.collection("Passenger").document(PassngerID)
                    .set(data, SetOptions.merge())
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Saved. Moving to next screen...", Toast.LENGTH_SHORT).show();

                        Map<String, Object> requestMap = new HashMap<>();
                        requestMap.put("passengerId", PassngerID); // Store the ID with a key

                        db.collection("PassRequest").add(requestMap).addOnSuccessListener(documentReference -> {
                            passrequestId = documentReference.getId();
                            Intent intent = new Intent(PassPersonalDetails.this, PassAddressDetails.class);
                            intent.putExtra("passrequestId", passrequestId); // pass mobile to next screen
                            startActivity(intent);
                            Toast.makeText(this, "Pass Request ID: " + passrequestId, Toast.LENGTH_SHORT).show();
                        });


                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error saving: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void autofillDetails() {
        db.collection("Passenger").document(PassngerID)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        etName.setText(documentSnapshot.getString("Name"));
                        etDob.setText(documentSnapshot.getString("DOB"));
                        etEmail.setText(documentSnapshot.getString("Email"));
                        etMobile.setText(documentSnapshot.getString("Contact_no"));

                        String gender = documentSnapshot.getString("Gender");
                        if ("Male".equalsIgnoreCase(gender)) {
                            radioGender.check(R.id.radioMale);
                        } else if ("Female".equalsIgnoreCase(gender)) {
                            radioGender.check(R.id.radioFemale);
                        }
                    }
                });
    }
}