package com.example.busmanagermentapplicationfinal.conductor;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.*;

import java.util.*;

public class ConductorEditProfile extends AppCompatActivity {

    EditText etContact, etFirstName, etLastName, etDOB, etGender, etAddress, etEmail;
    Button btnUpdate;
    FirebaseFirestore db;
    String documentId = "";
    String oldContact = "8777777777"; // Hardcoded for fetching

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_conductor_editprofile);

        db = FirebaseFirestore.getInstance();

//        etContact = findViewById(R.id.etContact);
//        etFirstName = findViewById(R.id.etFirstName);
//        etLastName = findViewById(R.id.etLastName);
//        etDOB = findViewById(R.id.etDOB);
//        etGender = findViewById(R.id.etGender);
//        etAddress = findViewById(R.id.etAddress);
//        etEmail = findViewById(R.id.etEmail);
//        btnUpdate = findViewById(R.id.btnUpdate);

        etContact.setText(oldContact); // Set default contact

        fetchConductorData();

        etDOB.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, day) -> {
                String date = String.format("%04d-%02d-%02d", year, month + 1, day);
                etDOB.setText(date);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        });

        btnUpdate.setOnClickListener(v -> updateConductorData());
    }

    private void fetchConductorData() {
        db.collection("User")
                .whereEqualTo("Contact_no", oldContact)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        documentId = doc.getId();

                        etFirstName.setText(doc.getString("FirstName"));
                        etLastName.setText(doc.getString("LastName"));
                        etDOB.setText(doc.getString("Date_Of_Birth"));
                        etGender.setText(doc.getString("Gender"));
                        etAddress.setText(doc.getString("Address"));
                        etEmail.setText(doc.getString("Email"));

                        Toast.makeText(this, "Data fetched", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Conductor not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateConductorData() {
        if (documentId.isEmpty()) {
            Toast.makeText(this, "No data loaded to update", Toast.LENGTH_SHORT).show();
            return;
        }

        String contact = etContact.getText().toString().trim();
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String dob = etDOB.getText().toString().trim();
        String gender = etGender.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(contact) || TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName)
                || TextUtils.isEmpty(dob) || TextUtils.isEmpty(gender)
                || TextUtils.isEmpty(address) || TextUtils.isEmpty(email)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("Contact_no", contact);
        updatedData.put("FirstName", firstName);
        updatedData.put("LastName", lastName);
        updatedData.put("Date_Of_Birth", dob);
        updatedData.put("Gender", gender);
        updatedData.put("Address", address);
        updatedData.put("Email", email);

        db.collection("User").document(documentId)
                .update(updatedData)
                .addOnSuccessListener(aVoid -> {
                    oldContact = contact;
                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
