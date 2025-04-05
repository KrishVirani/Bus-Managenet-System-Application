package com.example.busmanagementapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcherOwner;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;

import java.util.Objects;

public class Counductor_DrawerActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    public NavigationView navigationView;
    TextView tv_snv;
    ActionBarDrawerToggle toggle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_counductor_drawer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        ControlIntilization("Conductor Dashboard");
    }
    void ControlIntilization(String title)
    {
        // Set up Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // 🔴 Important! This enables ActionBar methods
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false); // Hide default title

        drawerLayout = findViewById(R.id.main);
        navigationView = findViewById(R.id.conductor_nav_side);
        tv_snv = findViewById(R.id.tx_snv);
        tv_snv.setText(title);

        // Enable back button
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }

        // Set up Drawer Toggle
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        //change color of drawer icon
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(android.R.color.white));

        // Handle navigation item clicks
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();
                // Load selected fragment
                if (itemId == R.id.nav_Home) {
                    selectedFragment = new Conductor_HomeFragment();
                    tv_snv.setText("Home");
                }
                else if (itemId == R.id.nav_book_ticket) {
                    selectedFragment = new Conductor_BookTIcketFragment();
                    tv_snv.setText("Book Ticket ");
                }
                else if (itemId == R.id.nav_view_bookings) {
                    selectedFragment = new Conductor_ViewBookingFragment();
                    tv_snv.setText("View Booking");
                }
                else if (itemId == R.id.nav_cancel_ticket) {
//                    selectedFragment = new Conductor_CancelTicketFragment();
                }
                else if (itemId == R.id.nav_live_location) {
//                    selectedFragment = new Conductor_LiveLocationFragment();
                }
                else if (itemId == R.id.nav_fare_calculator) {
//                    selectedFragment = new Conductor_FareCalculatorFragment();
                }
                else if (itemId == R.id.nav_logout) {
                    SharedPreferences pref=getSharedPreferences("LoginPref",MODE_PRIVATE);
                    SharedPreferences.Editor editor=pref.edit();
                    editor.remove("Email");
                    editor.commit();

                    Intent intent=new Intent(Counductor_DrawerActivity.this,MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }
                // Load selected fragment
                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();

                }



                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        getOnBackPressedDispatcher();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (toggle.onOptionsItemSelected(item))
        {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}