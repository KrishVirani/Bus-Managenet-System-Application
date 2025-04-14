package com.example.busmanagermentapplicationfinal.passenger;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.busmanagermentapplicationfinal.R;
import com.example.busmanagermentapplicationfinal.conductor.Conductor_TicketBooking;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class PassengerBaseActivity extends AppCompatActivity {
    protected FrameLayout frameLayout;
    BottomNavigationView bottomNavigationView;
    TextView toolbarTitle;
    @SuppressLint({"MissingInflatedId", "NonConstantResourceId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_passenger_base);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        frameLayout = findViewById(R.id.passenger_frame_container);
        bottomNavigationView = findViewById(R.id.passenger_bottom_nav);
        toolbarTitle = findViewById(R.id.toolbar_title);
        // Handle navigation clicks if needed globally
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                startActivity(new Intent(this, HomePage.class));
                return true;

            }

            else if (id == R.id.nav_bookings) {
                startActivity(new Intent(this, view_passenger_Booking.class));
                return true;

            } else if (id == R.id.nav_livelocation) {
//                startActivity(new Intent(this, Livelocation.class));
//                return true;

            } else if (id == R.id.nav_myaccount) {
//                startActivity(new Intent(this, PassengerAccountActivity.class));
                return true;
            }

            return false;
        });
    }
    public void setContentLayout(int layoutId) {
        LayoutInflater.from(this).inflate(layoutId, frameLayout, true);
    }
}