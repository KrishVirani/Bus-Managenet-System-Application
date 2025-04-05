package com.example.busmanagementapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class login_form extends AppCompatActivity {
    EditText txtEmail, txtPassword;
    Button login;
    ImageView back, togglePasswordVisibility;
    boolean isPasswordVisible = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_form);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ConrolIntialization();
        ButtonClick();
    }

    private void ConrolIntialization()
    {
        txtEmail = findViewById(R.id.email);
        txtPassword = findViewById(R.id.password);
        login = findViewById(R.id.login_button);
        back = findViewById(R.id.back_button);
        togglePasswordVisibility = findViewById(R.id.togglePasswordVisibility);
        SharedPreferences pref=getSharedPreferences("LoginPref",MODE_PRIVATE);
    }

    private void ButtonClick()
    {
        // Toggle Password Visibility
        togglePasswordVisibility.setOnClickListener(v -> {
            if (isPasswordVisible) {
                txtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                togglePasswordVisibility.setImageResource(R.drawable.icons8_eye_16);
            } else {
                txtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                togglePasswordVisibility.setImageResource(R.drawable.icons8_invisible_16);
            }
            isPasswordVisible = !isPasswordVisible;
            txtPassword.setSelection(txtPassword.getText().length()); // Move cursor to the end
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = txtEmail.getText().toString().trim();
                String password = txtPassword.getText().toString().trim();

                // Check for empty fields
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(login_form.this, "Please fill in both fields.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate email format
                if (!isValidEmail(email)) {
                    Toast.makeText(login_form.this, "Invalid email format", Toast.LENGTH_SHORT).show();
                    return;
                }

                //Validate password
//                if (!password.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{6,}$")) {
//                    Toast.makeText(login_form.this,"Password must have 1 uppercase, 1 lowercase, 1 digit, 1 special character, and be at least 6 characters long!",Toast.LENGTH_SHORT).show();
//                }

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("User")
                        .whereEqualTo("Email", email.trim())
                        .get()
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful() && !task.getResult().isEmpty()) {
//                                Toast.makeText(login_form.this,email,Toast.LENGTH_SHORT).show();
                                loginUser(email, password);
                            } else {

                                Toast.makeText(login_form.this, "Email not registered!", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

    }

    private void loginUser(String email, String password) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("User")
                .whereEqualTo("Email", email) // Find the user by email
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        String storedPassword = documentSnapshot.getString("Password");

                        if (storedPassword != null && storedPassword.equals(password)) {
                            String userType = documentSnapshot.getString("UserType");

                            if ("Passenger".equals(userType)) {
                                Toast.makeText(login_form.this, "Passenger Dashboard", Toast.LENGTH_SHORT).show();
                            } else if ("Conductor".equals(userType)) {

                                //Store in shared pref
                                SharedPreferences pref=getSharedPreferences("LoginPref",MODE_PRIVATE);
                                SharedPreferences.Editor editor=pref.edit();
                                editor.putString("Email",email);
                                editor.commit();

//                                Toast.makeText(login_form.this, "Conductor Dashboard", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(login_form.this, Counductor_DrawerActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(login_form.this, "User type not recognized!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(login_form.this, "Incorrect password!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(login_form.this, "User not found!", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(login_form.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }



    // Email validation using regex
    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
}