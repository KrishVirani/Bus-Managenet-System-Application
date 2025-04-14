package com.example.busmanagermentapplicationfinal.conductor;

import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.busmanagermentapplicationfinal.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashSet;
import java.util.Set;

public class conductor_view_Check_booking extends ConductorBaseActivity {

    private EditText etBookingId;
    TextView tvNoBookingMessage;
    private Button btnSearch;
    private LinearLayout resultContainer;
    private FirebaseFirestore db;
    ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_conductor_view_check_booking);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setupDrawer(R.layout.activity_conductor_view_check_booking);
        toolbarTitle.setText("View Booking");

        etBookingId = findViewById(R.id.etBookingId);
        btnSearch = findViewById(R.id.btnSearch);
        resultContainer = findViewById(R.id.resultContainer);
        db = FirebaseFirestore.getInstance();
        backButton = findViewById(R.id.btnback);
        tvNoBookingMessage = findViewById(R.id.tvNoBookingMessage);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnSearch.setOnClickListener(v -> {
            String idStr = etBookingId.getText().toString().trim();
            if (TextUtils.isEmpty(idStr)) {
                Toast.makeText(this, "Enter Booking ID", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                int bookingId = Integer.parseInt(idStr);
                fetchBookingById(bookingId);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid Booking ID", Toast.LENGTH_SHORT).show();
            }
        });

        etBookingId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    resultContainer.removeAllViews(); // Clear UI when input is cleared
                }
                else{
                    String idStr = etBookingId.getText().toString().trim();
                    try {
                        int bookingId = Integer.parseInt(idStr);
                        fetchBookingById(bookingId);
                    } catch (NumberFormatException e) {
                        Toast.makeText(getApplicationContext(), "Invalid Booking ID", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

    }

    private void fetchBookingById(int bookingId) {
        db.collection("OnlineTicketBooking")
                .whereEqualTo("ID", bookingId)
                .get()
                .addOnSuccessListener(snapshots -> {
                    resultContainer.removeAllViews();
                    Log.d("FIRESTORE_BOOKINGS", "Found " + snapshots.size() + " matching bookings");

                    if (snapshots.isEmpty()) {
                        Toast.makeText(this, "No booking found", Toast.LENGTH_SHORT).show();
                        tvNoBookingMessage.setVisibility(View.VISIBLE);
                        return;
                    }
                    // Hide the message if results are found
                    tvNoBookingMessage.setVisibility(View.GONE);
                    Set<String> shownDocIds = new HashSet<>();

                    for (DocumentSnapshot doc : snapshots) {
                        if (shownDocIds.contains(doc.getId())) continue;
                        shownDocIds.add(doc.getId());

                        String name = doc.getString("Name");
                        String gender = doc.getString("Gender");
                        String email = doc.getString("Email");
                        String contact = String.valueOf(doc.getLong("Contact_No"));
                        String dob = doc.getString("Date_Of_Birth");
                        String dateOfBooking = doc.getString("Date_of_booking");
                        Long seatNo = doc.getLong("Seats_Number");

                        DocumentReference busRef = doc.getDocumentReference("Bus_id");
                        DocumentReference fromRef = doc.getDocumentReference("From");
                        DocumentReference toRef = doc.getDocumentReference("To");

                        fetchRefDetailsAndDisplay(busRef, fromRef, toRef, name, gender, email, contact, dob, dateOfBooking, seatNo);
                    }
                });
    }

    private void fetchRefDetailsAndDisplay(DocumentReference busRef, DocumentReference fromRef, DocumentReference toRef,
                                           String name, String gender, String email, String contact,
                                           String dob, String dateOfBooking, Long seatNo) {

        Task<DocumentSnapshot> busTask = busRef.get();
        Task<DocumentSnapshot> fromTask = fromRef.get();
        Task<DocumentSnapshot> toTask = toRef.get();

        Tasks.whenAllSuccess(busTask, fromTask, toTask).addOnSuccessListener(tasks -> {
            DocumentSnapshot busSnap = (DocumentSnapshot) tasks.get(0);
            DocumentSnapshot fromSnap = (DocumentSnapshot) tasks.get(1);
            DocumentSnapshot toSnap = (DocumentSnapshot) tasks.get(2);

            String busName = busSnap.getString("BusName");
            String fromName = fromSnap.getString("Name");
            String toName = toSnap.getString("Name");

            addResultCard(busName, fromName, toName, dateOfBooking, seatNo, name, gender, email, contact, dob);
        });
    }

    private void addResultCard(String busName, String fromName, String toName, String bookingDate, Long seatNo,
                               String name, String gender, String email, String contact, String dob) {

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(32, 32, 32, 32);
        card.setBackgroundResource(R.drawable.white_rounded_card);

        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, 32);
        card.setLayoutParams(cardParams);

        TextView tvBusInfo = new TextView(this);
        tvBusInfo.setText("Bus: " + busName + "\nRoute: " + fromName + " to " + toName);
        tvBusInfo.setTextSize(16);
        tvBusInfo.setTypeface(null, Typeface.BOLD);

        TextView tvDateSeat = new TextView(this);
        tvDateSeat.setText("Booking Date: " + bookingDate + " | Seat No: " + seatNo);

        TextView tvPassenger = new TextView(this);
        tvPassenger.setText("Passenger: " + name + " (" + gender + ")");

        TextView tvContact = new TextView(this);
        tvContact.setText("Email: " + email + "\nContact: " + contact);

        TextView tvDOB = new TextView(this);
        tvDOB.setText("DOB: " + dob);

        card.addView(tvBusInfo);
        card.addView(tvDateSeat);
        card.addView(tvPassenger);
        card.addView(tvContact);
        card.addView(tvDOB);

        resultContainer.addView(card);
    }
}
