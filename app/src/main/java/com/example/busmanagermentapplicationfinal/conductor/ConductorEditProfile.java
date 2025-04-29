package com.example.busmanagermentapplicationfinal.conductor;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.busmanagermentapplicationfinal.R;
import com.google.firebase.firestore.*;

import java.util.*;

public class ConductorEditProfile extends AppCompatActivity {

    EditText etContact, etFirstName, etLastName, etDOB, etGender, etAddress, etEmail;
    Button btnUpdate;
    FirebaseFirestore db;
    String documentId = "";
    String oldContact = ""; // Hardcoded for fetching

    String conductorId = "299qvqYeCwMIXqO7E5Fy";
    ImageView backButton;
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

//        etContact.setText(oldContact); // Set default contact
//
//        fetchConductorData();

        backButton = findViewById(R.id.backButton2);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        db.collection("User").document(conductorId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        oldContact = documentSnapshot.getString("Contact_no");
                        etContact.setText(oldContact); // Now set it after fetching
                        fetchConductorData(); // Fetch the rest after oldContact is loaded
                    } else {
                        Toast.makeText(this, "Conductor not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());


        etDOB.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.YEAR, -18); // 18 years before today

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePicker = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
                String date = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
                etDOB.setText(date);
            }, year, month, day);

            datePicker.getDatePicker().setMaxDate(calendar.getTimeInMillis()); // restrict future dates
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

        if (!Patterns.PHONE.matcher(contact).matches() || contact.length() < 10) {
            Toast.makeText(this, "Invalid contact number", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String[] parts = dob.split("-");
            int year = Integer.parseInt(parts[0]);
            Calendar dobCalendar = Calendar.getInstance();
            dobCalendar.set(Calendar.YEAR, year);
            dobCalendar.set(Calendar.MONTH, Integer.parseInt(parts[1]) - 1);
            dobCalendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[2]));

            Calendar today = Calendar.getInstance();
            today.add(Calendar.YEAR, -18);
            if (dobCalendar.after(today)) {
                Toast.makeText(this, "Conductor must be at least 18 years old", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception e) {
            Toast.makeText(this, "Invalid Date of Birth format", Toast.LENGTH_SHORT).show();
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
