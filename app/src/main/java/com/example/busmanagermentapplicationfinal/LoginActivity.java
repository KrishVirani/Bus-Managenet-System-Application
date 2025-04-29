package com.example.busmanagermentapplicationfinal;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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

import com.example.busmanagermentapplicationfinal.Otp_page;
import com.example.busmanagermentapplicationfinal.R;
import com.example.busmanagermentapplicationfinal.SignUpPage;

public class LoginActivity extends AppCompatActivity {

    private EditText contactEditText;
    private Button loginButton;
    private TextView tvError, signupText;

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
        loginButton = findViewById(R.id.btnLogin);

        signupText = findViewById(R.id.signup);

        signupText.setOnClickListener(view -> {
            Intent intent = new Intent(LoginActivity.this, SignUpPage.class);
            startActivity(intent);
            finish();
        });

        loginButton.setOnClickListener(v -> {
            String contact = contactEditText.getText().toString().trim();

            if (contact.isEmpty()) {
                Toast.makeText(getApplicationContext(), "Please enter contact number", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate: 10 digits starting with 6, 7, 8, or 9
            if (!contact.matches("^[6-9]\\d{9}$")) {
                Toast.makeText(getApplicationContext(), "Invalid contact number", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check internet
            if (!isInternetAvailable()) {
                Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                return;
            }

            // Redirect to OTP screen
            Intent intent = new Intent(LoginActivity.this, Otp_page.class);
            intent.putExtra("Contact_no", contact);
            startActivity(intent);
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


}
