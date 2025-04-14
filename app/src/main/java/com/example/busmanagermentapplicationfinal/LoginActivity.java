package com.example.busmanagermentapplicationfinal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import com.example.busmanagermentapplicationfinal.passenger.* ;
import com.example.busmanagermentapplicationfinal.conductor.*;
import com.example.busmanagermentapplicationfinal.driver.*;

public class LoginActivity extends AppCompatActivity {

    private EditText contactEditText, passwordEditText;
    TextView signupedit;
    private Button loginButton;
    private TextView tvError;
    private FirebaseFirestore db;


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        contactEditText = findViewById(R.id.Contact);
        passwordEditText = findViewById(R.id.Password);
        loginButton = findViewById(R.id.btnLogin);
        tvError = findViewById(R.id.tvError);
        signupedit = findViewById(R.id.signup);

        signupedit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignUpPage.class);
                startActivity(intent);
                finish();
            }
        });

        FirebaseApp.initializeApp(this);
        db = FirebaseFirestore.getInstance();

        loginButton.setOnClickListener(v -> {
            String contact = contactEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString();

            if (contact.isEmpty() || password.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!contact.matches("\\d{10}")) {
                Toast.makeText(getApplicationContext(), "Contact number must be exactly 10 digits", Toast.LENGTH_SHORT).show();
                return;
            }

            else {
                tvError.setVisibility(View.GONE);
                checkPassengerCollection(contact, password);
            }

            if (!isInternetAvailable()) {
                Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                return;
            }



        });
    }

    private boolean isInternetAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                NetworkCapabilities nc = cm.getNetworkCapabilities(cm.getActiveNetwork());
                return nc != null && (
                        nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                                nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                                nc.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                );
            } else {
                NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                return activeNetwork != null && activeNetwork.isConnected();
            }
        }
        return false;
    }

    private void checkPassengerCollection(String contact, String password) {
        Toast.makeText(getApplicationContext(), "method call", Toast.LENGTH_SHORT).show();

//        long contactNumber;
        try {
//            contactNumber = Long.parseLong(contact); // Convert to number
        } catch (NumberFormatException e) {
            Toast.makeText(getApplicationContext(), "Invalid contact format", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Passenger")
                .whereEqualTo("Contact_no", contact)  // use number, not string
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(LoginActivity.this,"found in Passenger", Toast.LENGTH_SHORT).show();
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        String storedPassword = doc.getString("Password");

                        if (storedPassword != null && storedPassword.equals(password)) {

                            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("PassangerId", doc.getId());  // or doc.getId()
                            editor.apply();

                            startActivity(new Intent(LoginActivity.this,  PassangerDashbord.class));
                            Toast.makeText(getApplicationContext(), "Password is correct (Passenger)", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            tvError.setText("Incorrect password");
                            tvError.setVisibility(View.VISIBLE);
                        }
                    } else {
                        //Toast.makeText(getApplicationContext(), "Not found in Passenger", Toast.LENGTH_SHORT).show();
                        checkUserCollection(contact, password);
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    private void checkUserCollection(String contact, String password) {
        db.collection("User")
                .whereEqualTo("Contact_no", contact)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        String storedPassword = doc.getString("Password");
                        String userType = doc.getString("UserType");

                        if (storedPassword != null && storedPassword.equals(password)) {

                            SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                            SharedPreferences.Editor editor = prefs.edit();


//                            Toast.makeText(getApplicationContext(), "pasword is correct gresi", Toast.LENGTH_SHORT).show();
                            if ("Conductor".equalsIgnoreCase(userType)) {
                                editor.putString("ConductorId", doc.getId());  // or doc.getId()
                                editor.apply();
                                startActivity(new Intent(LoginActivity.this, ConductorDashbord.class));
                            } else if ("Driver".equalsIgnoreCase(userType)) {
                                editor.putString("DriverId", doc.getId());  // or doc.getId()
                                editor.apply();
                                startActivity(new Intent(LoginActivity.this, DriverDashbord.class));
                            } else {
                                Toast.makeText(this, "Unknown user type", Toast.LENGTH_SHORT).show();
                            }
                            finish();
                        } else {
                            tvError.setText("Incorrect password");
                            tvError.setVisibility(View.VISIBLE);
                        }
                    }
                    else {
                        Toast.makeText(this, "Account not found. Redirecting to Sign Up.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, SignUpPage.class));
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
