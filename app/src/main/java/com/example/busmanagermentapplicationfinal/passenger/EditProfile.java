package com.example.busmanagermentapplicationfinal.passenger;

import android.app.DatePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.busmanagermentapplicationfinal.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class EditProfile extends AppCompatActivity {

    EditText etContact, etName, etDOB;
    Button btnUpdate;
    FirebaseFirestore db;
    String passengerId = "SIM1VaALQGeIVkckUhsBand";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        db = FirebaseFirestore.getInstance();

        fetchPassengerData();
        etContact = findViewById(R.id.etContact);
        etName = findViewById(R.id.etName);
        etDOB = findViewById(R.id.etDOB);
        btnUpdate = findViewById(R.id.btnUpdate);
        // Show date picker on DOB click
        etDOB.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.YEAR, -18); // Minimum 18 years old

            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
                String selectedDate = String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear);
                etDOB.setText(selectedDate);
            }, year, month, day);

            dialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
            dialog.show();
        });


        // Update button action
        btnUpdate.setOnClickListener(v -> {
            if (validateFields()) {
                updatePassengerData();
            }
        });
    }

    private void fetchPassengerData() {
        DocumentReference docRef = db.collection("Passenger").document(passengerId);
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String contact = documentSnapshot.getString("contact");
                        String name = documentSnapshot.getString("name");
                        String dob = documentSnapshot.getString("dob");

                        etContact.setText(contact);
                        etName.setText(name);
                        etDOB.setText(dob);

                        Toast.makeText(this, "Data loaded", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Passenger not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    private boolean validateFields() {
        String name = etName.getText().toString().trim();
        String dob = etDOB.getText().toString().trim();

        if (name.isEmpty() || dob.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return false;
        }

        // ✅ Name should contain only letters and spaces
        if (!name.matches("^[a-zA-Z\\s]+$")) {
            Toast.makeText(this, "Name should contain only letters", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (name.length() < 3) {
            Toast.makeText(this, "Name should be at least 3 characters", Toast.LENGTH_SHORT).show();
            return false;
        }

        // ✅ Validate date format: dd/MM/yyyy
        if (!dob.matches("\\d{2}/\\d{2}/\\d{4}")) {
            Toast.makeText(this, "Invalid date format (dd/MM/yyyy)", Toast.LENGTH_SHORT).show();
            return false;
        }

        // ✅ Age validation (must be 18 or older)
        String[] dobParts = dob.split("/");
        int day = Integer.parseInt(dobParts[0]);
        int month = Integer.parseInt(dobParts[1]) - 1; // Calendar.MONTH is 0-based
        int year = Integer.parseInt(dobParts[2]);

        Calendar dobCalendar = Calendar.getInstance();
        dobCalendar.set(year, month, day);

        Calendar today = Calendar.getInstance();

        int age = today.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR);
        if (today.get(Calendar.MONTH) < dobCalendar.get(Calendar.MONTH) ||
                (today.get(Calendar.MONTH) == dobCalendar.get(Calendar.MONTH) &&
                        today.get(Calendar.DAY_OF_MONTH) < dobCalendar.get(Calendar.DAY_OF_MONTH))) {
            age--;
        }

        if (age < 18) {
            Toast.makeText(this, "You must be at least 18 years old", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }


    private void updatePassengerData() {
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("name", etName.getText().toString().trim());
        updatedData.put("dob", etDOB.getText().toString().trim());
        updatedData.put("contact", etContact.getText().toString().trim()); // Also allow contact update

        db.collection("Passenger").document(passengerId)
                .update(updatedData)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

}
