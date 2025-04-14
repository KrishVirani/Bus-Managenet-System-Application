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
        setupBottomNavigation();
        updateNavigationState();
//        bottomNavigationView.setOnItemSelectedListener(item -> {
//            int id = item.getItemId();
//
//            if (id == R.id.nav_home) {
//                startActivity(new Intent(this, HomePage.class));
//            }
//
//            else if (id == R.id.nav_bookings) {
//                startActivity(new Intent(this, view_passenger_Booking.class));
//                bottomNavigationView.setSelectedItemId(R.id.nav_bookings);
//            } else if (id == R.id.nav_livelocation) {
//                startActivity(new Intent(this, Live_Location_BusList.class));
//
//            } else if (id == R.id.nav_myaccount) {
////                startActivity(new Intent(this, PassengerAccountActivity.class));
//            }
//
//            return false;
//        });
    }
    private void setupBottomNavigation() {
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            Class<?> destination = null;

            if (id == R.id.nav_home) {
                destination = HomePage.class;
            } else if (id == R.id.nav_bookings) {
                destination = view_passenger_Booking.class;
            } else if (id == R.id.nav_livelocation) {
                destination = Live_Location_BusList.class;
            } else if (id == R.id.nav_myaccount) {
                // Handle account navigation
            }

            if (destination != null && !this.getClass().equals(destination)) {
                Intent intent = new Intent(this, destination);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                overridePendingTransition(0, 0); // Remove animation
                return true;
            }

            return false;
        });
    }
    protected void setSelectedItem(int itemId) {
        bottomNavigationView.setSelectedItemId(itemId);
    }
    protected void updateNavigationState() {
        if (this instanceof HomePage) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        } else if (this instanceof view_passenger_Booking) {
            bottomNavigationView.setSelectedItemId(R.id.nav_bookings);
        } else if (this instanceof Live_Location_BusList) {
            bottomNavigationView.setSelectedItemId(R.id.nav_livelocation);
        }
        // Add other activities as needed
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateNavigationState();
    }
    public void setContentLayout(int layoutId) {
        LayoutInflater.from(this).inflate(layoutId, frameLayout, true);
    }
}