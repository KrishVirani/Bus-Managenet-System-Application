package com.example.busmanagementapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class passengerSignup extends AppCompatActivity {

    public EditText txtfName,txtlname, txtEmail, txtPassword, txtConfPassword, txtPhone;
    private ImageView togglePasswordVisibility, toggleConfirmPasswordVisibility;
    private ShapeableImageView profilePicture;
    private Button Submit;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_passenger_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ConrolIntialization();
        ButtonClick();

        // Set Profile Picture
        profilePicture.setOnClickListener(v -> selectImageFromGallery());
    }

    private void ConrolIntialization()
    {
        txtfName = findViewById(R.id.txt_fname);
        txtlname = findViewById(R.id.txt_lname);
        txtEmail = findViewById(R.id.txt_email);
        txtPassword = findViewById(R.id.txt_password);
        txtConfPassword = findViewById(R.id.txt_conf_password);
        txtPhone = findViewById(R.id.txt_phone);
        togglePasswordVisibility = findViewById(R.id.togglePasswordVisibility);
        toggleConfirmPasswordVisibility = findViewById(R.id.toggleConfirmPasswordVisibility);
        profilePicture = findViewById(R.id.profilePicture);

        Submit=findViewById(R.id.signUpButton);
    }

    private void ButtonClick()
    {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Toggle Password Visibility
        togglePasswordVisibility.setOnClickListener(v -> {
            if (isPasswordVisible) {
                txtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                togglePasswordVisibility.setImageResource(R.drawable.icons8_eye_16); // Eye open icon
            } else {
                txtPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                togglePasswordVisibility.setImageResource(R.drawable.icons8_invisible_16); // Eye closed icon
            }
            isPasswordVisible = !isPasswordVisible;
            txtPassword.setSelection(txtPassword.getText().length()); // Move cursor to the end
        });

        // Toggle Confirm Password Visibility
        toggleConfirmPasswordVisibility.setOnClickListener(v -> {
            if (isConfirmPasswordVisible) {
                txtConfPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                toggleConfirmPasswordVisibility.setImageResource(R.drawable.icons8_eye_16); // Eye open icon
            } else {
                txtConfPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                toggleConfirmPasswordVisibility.setImageResource(R.drawable.icons8_invisible_16); // Eye closed icon
            }
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            txtConfPassword.setSelection(txtConfPassword.getText().length()); // Move cursor to the end
        });

        Submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fname = txtfName.getText().toString().trim();
                String lname = txtlname.getText().toString().trim();
                String email = txtEmail.getText().toString().trim();
                String password = txtPassword.getText().toString().trim();
                String confirmPassword = txtConfPassword.getText().toString().trim();
                String phone = txtPhone.getText().toString().trim();

                // Validate empty fields
                if (fname.isEmpty()|| lname.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || phone.isEmpty()) {
                    Toast.makeText(passengerSignup.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    return;
                }
                //Validate fName
                if (!fname.matches("[a-zA-Z]+") ) {
                    Toast.makeText(passengerSignup.this,"Only Letters are allow in First Name",Toast.LENGTH_SHORT).show();
                    return;
                }
                //Validate lname
                if (!lname.matches("[a-zA-Z]+") ) {
                    Toast.makeText(passengerSignup.this,"Only Letters are allow in Last Name",Toast.LENGTH_SHORT).show();
                    return;
                }


                // Validate email format
                if (!isValidEmail(email)) {
                    Toast.makeText(passengerSignup.this, "Invalid email format", Toast.LENGTH_SHORT).show();
                    return;
                }

                //Validate password
                if (!password.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@#$%^&+=!]).{6,}$")) {
                    Toast.makeText(passengerSignup.this,"Password must have 1 uppercase, 1 lowercase, 1 digit, 1 special character, and be at least 6 characters long!",Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!phone.matches("\\d{10}")) {
                    Toast.makeText(passengerSignup.this, "Please enter a valid 10-digit phone number.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate phone number format (using a simple pattern, adjust as needed)
                if (!isValidPhone(phone)) {
                    Toast.makeText(passengerSignup.this, "Invalid phone number", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if passwords match
                if (!password.equals(confirmPassword)) {
                    Toast.makeText(passengerSignup.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                db.collection("User")
                        .whereEqualTo("email", email)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            if (!queryDocumentSnapshots.isEmpty()) {
                                Toast.makeText(passengerSignup.this, "Email already registered", Toast.LENGTH_SHORT).show();
                            } else {
                                // Proceed with registration
                                registerUser(fname,lname, email, password, phone);
                            }
                        })
                        .addOnFailureListener(e -> {
                            Log.e("Firebase", "Error checking email", e);
                            Toast.makeText(passengerSignup.this, "Error checking email", Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }
    private void clearFields() {
        txtfName.setText("");
        txtlname.setText("");
        txtEmail.setText("");
        txtPassword.setText("");
        txtConfPassword.setText("");
        txtPhone.setText("");
    }
    //Insert Data into Firebase
    private void registerUser(String fname, String lname, String email, String password, String phone) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

//        if (auth.getCurrentUser() == null) {
//            Toast.makeText(this, "User not logged in. Please try again.", Toast.LENGTH_SHORT).show();
//            return;
//        }

//        String userId = Objects.requireNonNull(auth.getCurrentUser()).getUid(); // Safe user ID retrieval

        // Create a user object with default values as null and UserType as Passenger
        Map<String, Object> user = new HashMap<>();
        user.put("FirstName", fname);
        user.put("LastName", lname);
        user.put("Email", email);
        user.put("Password", password); // Use hashing in real apps
        user.put("UserName", null);
        user.put("Contact_no", phone);
        user.put("Address", null);
        user.put("Date_Of_Birth", null);
        user.put("Gender", null);
        user.put("Licence", null);
        user.put("UserType", "Passenger"); // Set UserType to Passenger

        // FIXED: Corrected Firestore collection name
        db.collection("User").document(email)
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(passengerSignup.this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                    clearFields();
                    startActivity(new Intent(passengerSignup.this, login_form.class));
                })
                .addOnFailureListener(e -> {
                    Log.e("Firebase", "Error registering user", e);
                    Toast.makeText(passengerSignup.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                });
    }



    // Email validation using regex
    private boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // Phone number validation using regex (this is a simple validation, you may adjust as needed)
    private boolean isValidPhone(String phone) {
        return !TextUtils.isEmpty(phone) && phone.matches("[0-9]{10}"); // 10-digit phone number
    }
    private void selectImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        try {
            imagePickerLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // Activity result launcher for image picking
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                        profilePicture.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );
}