package com.example.busmanagermentapplicationfinal;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.busmanagermentapplicationfinal.passenger.HomePage;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class SignUpPage extends AppCompatActivity {

    EditText edtName, edtDob, edtemail;
    View btnSignUp;
    FirebaseFirestore db;
    String contact_no;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_page);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize views
        edtName = findViewById(R.id.edtName);
        edtDob = findViewById(R.id.edtDob);
        edtemail = findViewById(R.id.edtemail);
        btnSignUp = findViewById(R.id.btnSignUp);

        contact_no = getIntent().getStringExtra("Contact_no");

        // Date picker on click
        edtDob.setFocusable(false);
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
                    String dob = selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    edtDob.setText(dob);
                },
                year, month, day
        );

        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private void validateAndSignUp() {
        String name = edtName.getText().toString().trim();
        String dob = edtDob.getText().toString().trim();
        String email = edtemail.getText().toString().trim();

        // Validate name: required, no digits allowed
        if (name.isEmpty()) {
            edtName.setError("Name is required");
            edtName.requestFocus();
            return;
        } else if (!name.matches("^[a-zA-Z\\s]+$")) {
            edtName.setError("Only letters allowed in name");
            edtName.requestFocus();
            return;
        }

        // Validate DOB: required
        if (dob.isEmpty()) {
            edtDob.setError("Date of Birth is required");
            edtDob.requestFocus();
            return;
        }

        // Validate email format
        if (email.isEmpty()) {
            edtemail.setError("Email is required");
            edtemail.requestFocus();
            return;
        } else if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$")) {
            edtemail.setError("Enter a valid email");
            edtemail.requestFocus();
            return;
        }

        // Store in Firestore (auto-generated ID)
        Map<String, Object> user = new HashMap<>();
        user.put("Name", name);
        user.put("DOB", dob);
        user.put("Email", email);
        user.put("Contact_no", contact_no);

        db.collection("Passenger")
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(SignUpPage.this, "Sign up successfully", Toast.LENGTH_SHORT).show();
                    clearFields();
                    startActivity(new Intent(SignUpPage.this, HomePage.class)); // Redirect to HomePage
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(SignUpPage.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    private void clearFields() {
        edtName.setText("");
        edtDob.setText("");
        edtemail.setText("");
    }
}
