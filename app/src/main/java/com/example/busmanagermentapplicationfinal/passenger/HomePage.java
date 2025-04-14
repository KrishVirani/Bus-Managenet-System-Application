package com.example.busmanagermentapplicationfinal.passenger;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.busmanagermentapplicationfinal.R;

import java.util.Calendar;

public class HomePage extends PassengerBaseActivity {

    EditText fromEditText, toEditText, dateEditText;
    Calendar calendar;
    Button searchBtn;

    static final int REQUEST_CODE_FROM = 1;
    static final int REQUEST_CODE_TO = 2;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_home_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setContentLayout(R.layout.activity_home_page);
        toolbarTitle.setText("Passenger Dashboard");

        fromEditText = findViewById(R.id.fromEditTextId);
        toEditText = findViewById(R.id.toEditTextId);
        dateEditText = findViewById(R.id.dateEditTextId);
        searchBtn = findViewById(R.id.searchNowBtnId);

        fromEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Intent intent = new Intent(HomePage.this, SelectStationsPage.class);
                    intent.putExtra("type", "from"); // or "to"
                    startActivityForResult(intent, REQUEST_CODE_FROM); // or REQUEST_CODE_TO
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    return true;
                }
                return false;
            }
        });

        toEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    Intent intent = new Intent(HomePage.this, SelectStationsPage.class);
                    intent.putExtra("type", "to"); // or "to"
                    startActivityForResult(intent, REQUEST_CODE_TO); // or REQUEST_CODE_TO
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    return true;
                }
                return false;
            }
        });

        dateEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    calendar = Calendar.getInstance();
                    int year = calendar.get(Calendar.YEAR);
                    int month = calendar.get(Calendar.MONTH);
                    int day = calendar.get(Calendar.DAY_OF_MONTH);

                    DatePickerDialog datepickerDialog=new DatePickerDialog(HomePage.this, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                            String dateStr = dayOfMonth + "/" + (month + 1) + "/" + year;
                            dateEditText.setText(dateStr);
                        }
                    },year,month,day);
                    datepickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);

                    datepickerDialog.show();
                    return true;
                }
                return false;
            }
        });


        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (fromEditText.getText().toString().isEmpty() || toEditText.getText().toString().isEmpty() || dateEditText.getText().toString().isEmpty()) {
                    Toast.makeText(HomePage.this, "Please select all fields", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent3 = new Intent(HomePage.this, BusSearchPage.class);
                    intent3.putExtra("from", fromEditText.getText().toString());
                    intent3.putExtra("to", toEditText.getText().toString());
                    intent3.putExtra("date", dateEditText.getText().toString());
                    startActivity(intent3);
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);

                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            String selectedStation = data.getStringExtra("selectedStation");

            if (requestCode == REQUEST_CODE_FROM) {
                fromEditText.setText(selectedStation);
            } else if (requestCode == REQUEST_CODE_TO) {
                toEditText.setText(selectedStation);
            }
        }
    }
}