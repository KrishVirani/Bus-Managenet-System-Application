package com.example.busmanagermentapplicationfinal.passenger;

import android.app.DatePickerDialog;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        db = FirebaseFirestore.getInstance();

        etContact = findViewById(R.id.etContact);
        etName = findViewById(R.id.etName);
        etDOB = findViewById(R.id.etDOB);
        btnUpdate = findViewById(R.id.btnUpdate);

        // Disable editing contact (it's the ID)
        etContact.setEnabled(false);

        // Auto-fill contact number (change if dynamic)
        etContact.setText("5555555555");

        // Fetch data when screen loads
        fetchPassengerData();

        // Show date picker on DOB click
        etDOB.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, day) -> {
                String selectedDate = String.format("%02d/%02d/%04d", day, month + 1, year);
                etDOB.setText(selectedDate);
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
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
        String contact = etContact.getText().toString();

        DocumentReference docRef = db.collection("Passenger").document(contact);
        docRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        etName.setText(documentSnapshot.getString("name"));
                        etDOB.setText(documentSnapshot.getString("dob"));
                        Toast.makeText(this, "Data loaded", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Passenger not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
        String contact = etContact.getText().toString();

        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("name", etName.getText().toString().trim());
        updatedData.put("dob", etDOB.getText().toString().trim());

        db.collection("Passenger").document(contact)
                .update(updatedData)
                .addOnSuccessListener(unused ->
                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
