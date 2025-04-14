package com.example.busmanagermentapplicationfinal;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SignUpPage extends AppCompatActivity {

    EditText edtName, edtDob, edtContact, edtPassword;
    View btnSignUp;
    FirebaseFirestore db;
    Calendar selectedDate = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_page);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        edtName = findViewById(R.id.edtName);
        edtDob = findViewById(R.id.edtDob);
        edtContact = findViewById(R.id.edtContact);
        edtPassword = findViewById(R.id.edtpassword);
        btnSignUp = findViewById(R.id.btnSignUp);

        edtDob.setFocusable(false); // Prevent keyboard popup
        // Show DatePicker when clicking on DOB field
        edtDob.setOnClickListener(v -> showDatePickerDialog());

        btnSignUp.setOnClickListener(view -> validateAndSignUp());
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                SignUpPage.this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    selectedDate.set(selectedYear, selectedMonth, selectedDay);
                    // Check if user is 18+
                    if (isUserAdult(selectedDate)) {
                        String dob = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                        edtDob.setText(dob);
                    } else {
                        edtDob.setText(""); // Clear invalid DOB
                        Toast.makeText(this, "You must be at least 18 years old.", Toast.LENGTH_SHORT).show();
                    }

                },
                year, month, day
        );


        // Prevent future dates
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }
    private boolean isUserAdult(Calendar dob) {
        Calendar today = Calendar.getInstance();
        today.add(Calendar.YEAR, -18); // Go back 18 years
        return dob.compareTo(today) <= 0;
    }
    private void validateAndSignUp() {
        String name = edtName.getText().toString().trim();
        String dob = edtDob.getText().toString().trim();
        String contact = edtContact.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();

        // Validation
        if (name.isEmpty()) {
            edtName.setError("Name is required");
            edtName.requestFocus();
            return;
        }
        if (dob.isEmpty()) {
            edtDob.setError("Date of Birth is required");
            edtDob.requestFocus();
            return;
        }
        if (!contact.matches("^[6-9]\\d{9}$") || contact.matches("(\\d)\\1{9}")) {
            edtContact.setError("Enter a valid contact starting with 6-9 and not all same digits");
            edtContact.requestFocus();
            return;
        }

        if (!password.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{6,}$")) {
            edtPassword.setError("Password must be 6 characters, include 1 letter, 1 number & 1 special character");
            edtPassword.requestFocus();
            return;
        }



        // Prepare data
        Map<String, Object> user = new HashMap<>();
        user.put("Name", name);
        user.put("DOB", dob);
        user.put("Contact_no", contact);
        user.put("Password", password);

        // Add to Firestore
        db.collection("Passenger").document(contact)
                .set(user)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(SignUpPage.this, "Sign up successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignUpPage.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                    clearFields();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(SignUpPage.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void clearFields() {
        edtName.setText("");
        edtDob.setText("");
        edtContact.setText("");
        edtPassword.setText("");
    }
}
