package com.example.busmanagermentapplicationfinal.passenger;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.busmanagermentapplicationfinal.R;
import com.google.firebase.firestore.FirebaseFirestore;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

public class add_passenger_details_Page extends AppCompatActivity implements PaymentResultListener{

    LinearLayout formContainer;
    Button payButton;
    List<String> selectedSeats;
    int totalFare;
    String scheduleId, busId, fromStationId, toStationId;
    FirebaseFirestore db;
    List<View> seatForms = new ArrayList<>();
    ImageView imgback4;
    String[] genderOptions = {"Male", "Female", "Other"};
    String passengerId="7NTNWfsOaHUnHNdagUTg";
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_passenger_details_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        imgback4 = findViewById(R.id.imgback4);
        imgback4.setOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        formContainer = findViewById(R.id.passengerFormContainer);
        payButton = findViewById(R.id.payButton);

        selectedSeats = getIntent().getStringArrayListExtra("selectedSeats");
        totalFare = getIntent().getIntExtra("totalFare", 0);
        scheduleId = getIntent().getStringExtra("scheduleId");
        busId = getIntent().getStringExtra("busId");
        fromStationId = getIntent().getStringExtra("fromStationId");
        toStationId = getIntent().getStringExtra("toStationId");

        payButton.setText("Pay (" + totalFare + ")");

        for (String seat : selectedSeats) {
            View formView = createPassengerForm(seat);
            seatForms.add(formView);
            formContainer.addView(formView);
        }

        payButton.setOnClickListener(v -> {
            if (validateAllForms()) {
                startPayment();
            }
        });

    }

    private View createPassengerForm(String seatNumber) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View formView = inflater.inflate(R.layout.rounded_passenger_form, null);

        TextView seatTitle = formView.findViewById(R.id.seatTitle);
        seatTitle.setText("Seat: " + seatNumber);

        EditText dobField = formView.findViewById(R.id.dobField);
        dobField.setFocusable(false); // Prevent keyboard on click
        dobField.setOnClickListener(v -> showDatePicker(dobField));

        return formView;
    }

    public void startPayment() {
        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_jr3QNNykkgQT1R");

        try {
            JSONObject options = new JSONObject();
            options.put("name", "Bus Booking");
            options.put("description", "Ticket Payment");
            options.put("currency", "INR");
            options.put("amount", totalFare * 100);

            checkout.open(this, options);

        } catch (Exception e) {
            Toast.makeText(this, "Error in payment: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }


    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // On payment success, store to Firestore
        if (resultCode == RESULT_OK) {
            saveBookingData("Upi"); // Or read from Razorpay result
        }
    }

    public void onPaymentSuccess(String razorpayPaymentID) {
        saveBookingData("Razorpay");
        Toast.makeText(this, "Payment Successful", Toast.LENGTH_SHORT).show();
    }

    public void onPaymentError(int code, String response) {
        Toast.makeText(this, "Payment Failed: " + response, Toast.LENGTH_LONG).show();
    }
    private void saveBookingData(String paymentMode) {
        for (View formView : seatForms) {
            EditText nameField = formView.findViewById(R.id.nameField);
            EditText addressField = formView.findViewById(R.id.addressField);
            EditText dobField = formView.findViewById(R.id.dobField);
            EditText contactField = formView.findViewById(R.id.contactField);
            RadioGroup genderGroup = formView.findViewById(R.id.genderGroup);

            String name = nameField.getText().toString().trim();
            String address = addressField.getText().toString().trim();
            String dob = dobField.getText().toString().trim();
            String contact = contactField.getText().toString().trim();
            int selectedGenderId = genderGroup.getCheckedRadioButtonId();

            if (name.isEmpty() || address.isEmpty() || dob.isEmpty() || contact.isEmpty() || selectedGenderId == -1) {
                Toast.makeText(this, "Please fill all fields correctly for all passengers.", Toast.LENGTH_LONG).show();
                return;
            }

            String gender = ((RadioButton) genderGroup.findViewById(selectedGenderId)).getText().toString();
            String seat = ((TextView) formView.findViewById(R.id.seatTitle)).getText().toString().replace("Seat: ", "");

            Map<String, Object> booking = new HashMap<>();
            booking.put("Name", name);
            booking.put("Contact_No", contact);
            booking.put("Date_Of_Birth", dob);
            booking.put("Gender", gender);
            booking.put("Seats_Number", seat);
            booking.put("Date_of_booking", new SimpleDateFormat("d/M/yyyy", Locale.getDefault()).format(new Date()));
            booking.put("ID", new Random().nextInt(9000) + 1000);
            booking.put("Bus_id", db.document("Bus_Details/" + busId));
            booking.put("ScheduleId", db.document("Schedule/" + scheduleId));
            booking.put("From", db.document("Station/" + fromStationId));
            booking.put("To", db.document("Station/" + toStationId));
            booking.put("Discount_id", db.document("Discount/8g2DLTMjfNN3MsyfLhvU"));
            booking.put("PassangerId", db.document("Passenger/" + passengerId));

            db.collection("OnlineTicketBooking").add(booking)
                    .addOnSuccessListener(docRef -> {
                        Map<String, Object> txn = new HashMap<>();
                        txn.put("Payment_mode", paymentMode);
                        txn.put("Total_amount", totalFare);
                        txn.put("OfflineBooking_id", db.document("OfflineTicketBooking/null"));
                        txn.put("OnlineBooking_id", docRef);

                        db.collection("Transaction").add(txn);
                    });
        }

        Toast.makeText(this, "Booking Successful!", Toast.LENGTH_SHORT).show();
        finish(); // Or go to confirmation screen
        Intent intent=new Intent(add_passenger_details_Page.this, HomePage.class);
        startActivity(intent);
    }

    private boolean validateAllForms() {
        for (View formView : seatForms) {
            EditText nameField = formView.findViewById(R.id.nameField);
            EditText addressField = formView.findViewById(R.id.addressField);
            EditText dobField = formView.findViewById(R.id.dobField);
            EditText contactField = formView.findViewById(R.id.contactField);
            RadioGroup genderGroup = formView.findViewById(R.id.genderGroup);

            String name = nameField.getText().toString().trim();
            String address = addressField.getText().toString().trim();
            String dob = dobField.getText().toString().trim();
            String contact = contactField.getText().toString().trim();
            int selectedGenderId = genderGroup.getCheckedRadioButtonId();

            if (name.isEmpty()) {
                nameField.setError("Name is required");
                nameField.requestFocus();
                return false;
            }

            if (address.isEmpty()) {
                addressField.setError("Address is required");
                addressField.requestFocus();
                return false;
            }

            if (dob.isEmpty()) {
                dobField.setError("Date of Birth is required");
                dobField.requestFocus();
                return false;
            } else {
                // Validate if DOB is at least 5 years ago
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    Date dobDate = sdf.parse(dob);
                    Calendar minValidDob = Calendar.getInstance();
                    minValidDob.add(Calendar.YEAR, -5);

                    if (dobDate != null && dobDate.after(minValidDob.getTime())) {
                        dobField.setError("DOB must be at least 5 years ago");
                        dobField.requestFocus();
                        return false;
                    }
                } catch (Exception e) {
                    dobField.setError("Invalid date format");
                    dobField.requestFocus();
                    return false;
                }
            }

            if (contact.isEmpty()) {
                contactField.setError("Contact number is required");
                contactField.requestFocus();
                return false;
            } else if (!contact.matches("\\d{10}")) {
                contactField.setError("Enter a valid 10-digit number");
                contactField.requestFocus();
                return false;
            }


            if (selectedGenderId == -1) {
                Toast.makeText(this, "Please select gender for all passengers", Toast.LENGTH_SHORT).show();
                return false;
            }
        }

        return true;
    }



    private void showDatePicker(EditText dobField) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.YEAR, -5); // 5 years before today
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
            Calendar selectedDate = Calendar.getInstance();
            selectedDate.set(year1, month1, dayOfMonth);

            Calendar minDate = Calendar.getInstance();
            minDate.add(Calendar.YEAR, -5);

            if (selectedDate.after(minDate)) {
                Toast.makeText(this, "Date of birth must be at least 5 years ago.", Toast.LENGTH_SHORT).show();
            } else {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                dobField.setText(sdf.format(selectedDate.getTime()));
            }
        }, year, month, day);

        // Set maximum date to 5 years before today
        datePickerDialog.getDatePicker().setMaxDate(calendar.getTimeInMillis());
        datePickerDialog.show();
    }


}