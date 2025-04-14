package com.example.busmanagermentapplicationfinal.driver;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.busmanagermentapplicationfinal.LoginActivity;
import com.example.busmanagermentapplicationfinal.R;
import com.example.busmanagermentapplicationfinal.conductor.ConductorDashbord;
import com.google.android.material.navigation.NavigationView;

public class DriverBaseActivity extends AppCompatActivity {
    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected Toolbar toolbar;
    TextView toolbarTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_driver_base);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void setupDrawer(int layoutResID) {
        DrawerLayout fullLayout = (DrawerLayout) getLayoutInflater().inflate(R.layout.activity_driver_base, null);
        FrameLayout frameLayout = fullLayout.findViewById(R.id.activity_content);
        getLayoutInflater().inflate(layoutResID, frameLayout, true);
        super.setContentView(fullLayout);

        drawerLayout = fullLayout;
        navigationView = fullLayout.findViewById(R.id.navigation_view);
        toolbar = fullLayout.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbarTitle = fullLayout.findViewById(R.id.toolbar_title);

        View headerView = navigationView.getHeaderView(0);
        TextView navHeaderRole = headerView.findViewById(R.id.nav_header_role);
        navHeaderRole.setText("Welcome Driver\nMahesh babu");

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Handle nav item click
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            int id = menuItem.getItemId();
            if (id == R.id.nav_dashboard) {
                startActivity(new Intent(this, ConductorDashbord.class));
            }
            else if (id == R.id.nav_bus_schedule) {
                startActivity(new Intent(this, Driver_Checkschedule.class));
            }
            else if (id == R.id.nav_Emargency_alert) {
                startActivity(new Intent(this, driver_create_emergency_alert.class));
            }
            else if (id == R.id.nav_logout) {
                startActivity(new Intent(this, LoginActivity.class));
                finish();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}