package com.example.busmanagermentapplicationfinal.driver;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.*;

import java.util.*;

public class DriverEditProfile extends AppCompatActivity {

    EditText etContact, etFirstName, etLastName, etDOB, etGender, etEmail, etAddress;
    Button btnUpdate;
    FirebaseFirestore db;
    String documentId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_driver_editprofile);

        db = FirebaseFirestore.getInstance();

//        etContact = findViewById(R.id.etContact);
//        etFirstName = findViewById(R.id.etFirstName);
//        etLastName = findViewById(R.id.etLastName);
//        etDOB = findViewById(R.id.etDOB);
//        etGender = findViewById(R.id.etGender);
//        etEmail = findViewById(R.id.etEmail);
//        etAddress = findViewById(R.id.etAddress); // bind the address field
//        btnUpdate = findViewById(R.id.btnUpdate);

        // Automatically set the contact number to 9999999998
        etContact.setText("9999999998");

        // Fetch driver data for the hardcoded contact number
        fetchDriverData();

        etDOB.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog datePicker = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                String date = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth);
                etDOB.setText(date);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
            datePicker.show();
        });

        btnUpdate.setOnClickListener(v -> updateDriverData());
    }

    private void fetchDriverData() {
        String contact = etContact.getText().toString().trim();
        if (contact.isEmpty()) {
            Toast.makeText(this, "Enter contact number", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("User")
                .whereEqualTo("Contact_no", contact)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot doc = querySnapshot.getDocuments().get(0);
                        documentId = doc.getId();

                        etFirstName.setText(doc.getString("FirstName"));
                        etLastName.setText(doc.getString("LastName"));
                        etDOB.setText(doc.getString("Date_Of_Birth"));
                        etGender.setText(doc.getString("Gender"));
                        etEmail.setText(doc.getString("Email"));
                        etAddress.setText(doc.getString("Address")); // fetch address

                        Toast.makeText(this, "Data fetched", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Driver not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateDriverData() {
        if (documentId.isEmpty()) {
            Toast.makeText(this, "No data loaded to update", Toast.LENGTH_SHORT).show();
            return;
        }

        String updatedContact = etContact.getText().toString().trim();

        Map<String, Object> updated = new HashMap<>();
        updated.put("Contact_no", updatedContact);
        updated.put("FirstName", etFirstName.getText().toString().trim());
        updated.put("LastName", etLastName.getText().toString().trim());
        updated.put("Date_Of_Birth", etDOB.getText().toString().trim());
        updated.put("Gender", etGender.getText().toString().trim());
        updated.put("Email", etEmail.getText().toString().trim());
        updated.put("Address", etAddress.getText().toString().trim()); // add address

        db.collection("User").document(documentId)
                .update(updated)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
