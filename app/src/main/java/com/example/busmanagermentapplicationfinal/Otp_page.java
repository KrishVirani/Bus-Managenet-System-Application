package com.example.busmanagermentapplicationfinal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.busmanagermentapplicationfinal.conductor.ConductorDashbord;
import com.example.busmanagermentapplicationfinal.driver.DriverDashbord;
import com.example.busmanagermentapplicationfinal.passenger.HomePage;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

public class Otp_page extends AppCompatActivity {

    EditText otp1, otp2, otp3, otp4, otp5, otp6;
    Button btnVerifyOtp, btnResendOtp;
    TextView txtphone;
    FirebaseFirestore db;
    String contact_no;
    FirebaseAuth mAuth;
    String verificationId;
    PhoneAuthProvider.ForceResendingToken resendToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_page);

        contact_no = getIntent().getStringExtra("Contact_no");
        txtphone = findViewById(R.id.txtPhone);
        txtphone.setText(contact_no);

        otp1 = findViewById(R.id.otp1);
        otp2 = findViewById(R.id.otp2);
        otp3 = findViewById(R.id.otp3);
        otp4 = findViewById(R.id.otp4);
        otp5 = findViewById(R.id.otp5);
        otp6 = findViewById(R.id.otp6);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        btnResendOtp = findViewById(R.id.btnResendOtp);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        setOtpInputs();
        sendVerificationCode("+91" + contact_no); // Add +91 or your country code

        btnVerifyOtp.setOnClickListener(v -> {
            String otp = otp1.getText().toString().trim() +
                    otp2.getText().toString().trim() +
                    otp3.getText().toString().trim() +
                    otp4.getText().toString().trim() +
                    otp5.getText().toString().trim() +
                    otp6.getText().toString().trim();

            if (otp.length() != 6) {
                Toast.makeText(this, "Please enter 6 digit OTP", Toast.LENGTH_SHORT).show();
                return;
            }

            verifyCode(otp);
        });

        btnResendOtp.setOnClickListener(v -> {
            resendVerificationCode("+91" + contact_no, resendToken);
        });
    }

   

    private void verifyCode(String code) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithCredential(credential);
    }

    private void signInWithCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        checkContactInDatabase(contact_no);
                    } else {
                        Toast.makeText(Otp_page.this, "OTP Verification Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void checkContactInDatabase(String contact) {
        db.collection("Passenger")
                .whereEqualTo("Contact_no", contact)
                .get()
                .addOnCompleteListener(taskPassenger -> {
                    if (taskPassenger.isSuccessful() && !taskPassenger.getResult().isEmpty()) {
                        goToHome();
                    } else {
                        db.collection("User")
                                .whereEqualTo("Contact_no", contact)
                                .get()
                                .addOnCompleteListener(taskUser -> {
                                    if (taskUser.isSuccessful() && !taskUser.getResult().isEmpty()) {
                                        String userType = taskUser.getResult().getDocuments().get(0).getString("UserType");

                                        if ("Driver".equalsIgnoreCase(userType)) {
                                            goToDriverDashboard();
                                        } else if ("Conductor".equalsIgnoreCase(userType)) {
                                            goToConductorDashboard();
                                        } else {
                                            goToHome();
                                        }
                                    } else {
                                        goToSignUp();
                                    }
                                });
                    }
                });
    }

    private void goToHome() {
        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();

        // Save contact number to SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Contact_no", contact_no);
        editor.apply();

        // Start HomePage and pass contact_no as extra
        startActivity(new Intent(Otp_page.this, HomePage.class)
                .putExtra("Contact_no", contact_no));
        finish();
    }


    private void goToDriverDashboard() {
        Toast.makeText(this, "Driver Login Successful", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(Otp_page.this, DriverDashbord.class)
                .putExtra("Contact_no", contact_no));
        finish();
    }

    private void goToConductorDashboard() {
        Toast.makeText(this, "Conductor Login Successful", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(Otp_page.this, ConductorDashbord.class)
                .putExtra("Contact_no", contact_no));
        finish();
    }

    private void goToSignUp() {
        Toast.makeText(this, "Contact not registered. Redirecting to Sign Up.", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(Otp_page.this, SignUpPage.class)
                .putExtra("Contact_no", contact_no));
        finish();
    }

    private void fillOtpFields(String code) {
        if (code.length() == 6) {
            otp1.setText(String.valueOf(code.charAt(0)));
            otp2.setText(String.valueOf(code.charAt(1)));
            otp3.setText(String.valueOf(code.charAt(2)));
            otp4.setText(String.valueOf(code.charAt(3)));
            otp5.setText(String.valueOf(code.charAt(4)));
            otp6.setText(String.valueOf(code.charAt(5)));
        }
    }

    private void setOtpInputs() {
        setUpOtpEditText(otp1, otp2, otp1);
        setUpOtpEditText(otp2, otp3, otp1);
        setUpOtpEditText(otp3, otp4, otp2);
        setUpOtpEditText(otp4, otp5, otp3);
        setUpOtpEditText(otp5, otp6, otp4);
        setUpOtpEditText(otp6, otp6, otp5);
    }

    private void setUpOtpEditText(final EditText current, final EditText next, final EditText previous) {
        current.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1) next.requestFocus();
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        current.setOnKeyListener((v, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN && current.getText().toString().isEmpty()) {
                previous.requestFocus();
                return true;
            }
            return false;
        });
    }
}
