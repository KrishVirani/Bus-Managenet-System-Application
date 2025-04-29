package com.example.busmanagermentapplicationfinal.driver;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.busmanagermentapplicationfinal.R;
import com.google.firebase.firestore.*;

import java.util.*;

public class DriverEditProfile extends AppCompatActivity {

    EditText etContact, etFirstName, etLastName, etDOB, etGender, etEmail, etAddress;
    Button btnUpdate;
    FirebaseFirestore db;
    String documentId = "";
    String driverId = "299qvqYeCwMIXqO7E5Fy"; // static driver ID


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_editprofile); // Ensure this line is uncommented

        etContact = findViewById(R.id.etContact);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etDOB = findViewById(R.id.etDOB);
        etGender = findViewById(R.id.etGender);
        etEmail = findViewById(R.id.etEmail);
        etAddress = findViewById(R.id.etAddress);
        btnUpdate = findViewById(R.id.btnUpdate);

// Fetch driver data
        fetchDriverData();

        etDOB.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.YEAR, -18); // Set max date to 18 years ago
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(this, (view, y, m, d) -> {
                String date = String.format("%04d-%02d-%02d", y, m + 1, d);
                etDOB.setText(date);
            }, year, month, day);

            datePicker.getDatePicker().setMaxDate(calendar.getTimeInMillis()); // Max date = today - 18 years
            datePicker.show();
        });

        btnUpdate.setOnClickListener(v -> {
            if (validateInputs()) {
                updateDriverData();
            }
        });

    }

    private boolean validateInputs() {
        String contact = etContact.getText().toString().trim();
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String dob = etDOB.getText().toString().trim();
        String gender = etGender.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (contact.isEmpty() || contact.length() != 10) {
            etContact.setError("Enter valid 10-digit contact number");
            return false;
        }

        if (firstName.isEmpty()) {
            etFirstName.setError("First name required");
            return false;
        }

        if (lastName.isEmpty()) {
            etLastName.setError("Last name required");
            return false;
        }

        if (dob.isEmpty()) {
            etDOB.setError("Date of birth required");
            return false;
        }

        if (gender.isEmpty()) {
            etGender.setError("Gender required");
            return false;
        }

        if (!email.isEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Invalid email format");
            return false;
        }

        if (address.isEmpty()) {
            etAddress.setError("Address required");
            return false;
        }

        return true;
    }

    private void fetchDriverData() {
        db.collection("User").document(driverId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        documentId = doc.getId();

                        etContact.setText(doc.getString("Contact_no"));
                        etFirstName.setText(doc.getString("FirstName"));
                        etLastName.setText(doc.getString("LastName"));
                        etDOB.setText(doc.getString("Date_Of_Birth"));
                        etGender.setText(doc.getString("Gender"));
                        etEmail.setText(doc.getString("Email"));
                        etAddress.setText(doc.getString("Address"));

                        Toast.makeText(this, "Data fetched", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Driver not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    private void updateDriverData() {
        String updatedContact = etContact.getText().toString().trim();

        Map<String, Object> updated = new HashMap<>();
        updated.put("Contact_no", updatedContact);
        updated.put("FirstName", etFirstName.getText().toString().trim());
        updated.put("LastName", etLastName.getText().toString().trim());
        updated.put("Date_Of_Birth", etDOB.getText().toString().trim());
        updated.put("Gender", etGender.getText().toString().trim());
        updated.put("Email", etEmail.getText().toString().trim());
        updated.put("Address", etAddress.getText().toString().trim());

        db.collection("User").document(driverId)
                .update(updated)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Profile updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
