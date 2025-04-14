package com.example.busmanagermentapplicationfinal.passenger;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.busmanagermentapplicationfinal.R;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class view_passenger_Booking extends PassengerBaseActivity {

    private LinearLayout bookingListContainer;
    private FirebaseFirestore db;
    private String staticPassengerId = "7NTNWfsOaHUnHNdagUTg";
    ImageView backButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_view_passenger_booking);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        setContentLayout(R.layout.activity_view_passenger_booking);
        toolbarTitle.setText("Passenger Booking");
        backButton = findViewById(R.id.btnbck);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        bookingListContainer = findViewById(R.id.bookingListContainer);
        db = FirebaseFirestore.getInstance();

        loadBookings();
    }

    private void loadBookings() {
        db.collection("OnlineTicketBooking")
                .whereEqualTo("PassangerId", db.document("Passenger/" + staticPassengerId))
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String busRefPath = doc.getDocumentReference("Bus_id").getPath();
                        String fromRefPath = doc.getDocumentReference("From").getPath();
                        String toRefPath = doc.getDocumentReference("To").getPath();
                        String date = doc.getString("Date_of_booking");
                        Long seatNum = doc.getLong("Seats_Number");

                        fetchDetailsAndAddCard(busRefPath, fromRefPath, toRefPath, date, seatNum);
                    }
                });
    }

    private void fetchDetailsAndAddCard(String busRefPath, String fromRefPath, String toRefPath, String date, Long seatNum) {
        Task<DocumentSnapshot> busTask = db.document(busRefPath).get();
        Task<DocumentSnapshot> fromTask = db.document(fromRefPath).get();
        Task<DocumentSnapshot> toTask = db.document(toRefPath).get();

        Tasks.whenAllSuccess(busTask, fromTask, toTask)
                .addOnSuccessListener(tasks -> {
                    DocumentSnapshot busSnap = (DocumentSnapshot) tasks.get(0);
                    DocumentSnapshot fromSnap = (DocumentSnapshot) tasks.get(1);
                    DocumentSnapshot toSnap = (DocumentSnapshot) tasks.get(2);

                    String busName = busSnap.getString("BusName");
                    String busType = busSnap.getString("BusType");
                    String fromName = fromSnap.getString("Name");
                    String toName = toSnap.getString("Name");

                    addBookingCard(busName, busType, fromName, toName, date, seatNum);

                });
    }

    private void addBookingCard(String busName, String busType, String fromName, String toName, String date, Long seatNum) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(32, 32, 32, 32);
        card.setBackgroundResource(R.drawable.white_rounded_card);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, 32);
        card.setLayoutParams(cardParams);

        TextView tvBusName = new TextView(this);
        tvBusName.setText(busName + " (" + busType + ")");
        tvBusName.setTextSize(16);
        tvBusName.setTypeface(null, Typeface.BOLD);

        TextView tvRoute = new TextView(this);
        tvRoute.setText("Route: " + fromName + " to " + toName); // ✅ Fixed here

        TextView tvDate = new TextView(this);
        tvDate.setText("Date: " + date);

        TextView tvSeat = new TextView(this);
        tvSeat.setText("Seat No: " + seatNum);

        card.addView(tvBusName);
        card.addView(tvRoute);
        card.addView(tvDate);
        card.addView(tvSeat);

        bookingListContainer.addView(card);
    }

}