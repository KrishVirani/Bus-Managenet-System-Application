package com.example.busmanagementapplication;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * A simple {@link Fragment} subclass.
 * Use the  factory method to
 * create an instance of this fragment.
 */
public class Conductor_ViewBookingFragment extends Fragment {

    private EditText edtBookingId;
    private Button btnSubmit;
    private TextView txtBookingDetails;
    private FirebaseFirestore db;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public Conductor_ViewBookingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conductor__view_booking, container, false);
        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize Views
        edtBookingId = view.findViewById(R.id.BookingId);
        btnSubmit = view.findViewById(R.id.submit);
        txtBookingDetails = view.findViewById(R.id.txtBookingDetails); // Add this TextView in XML

        // Set Button Click Listener
        btnSubmit.setOnClickListener(v -> fetchBookingById());

        return view;
    }

    // Method to fetch booking details based on ID
    private void fetchBookingById() {
        String bookingIdStr = edtBookingId.getText().toString().trim();

        if (bookingIdStr.isEmpty()) {
            Toast.makeText(getContext(), "Enter a Booking ID!", Toast.LENGTH_SHORT).show();
            return;
        }

        int bookingId = Integer.parseInt(bookingIdStr); // Convert String to Integer

        // Query Firestore for Booking Data
        db.collection("OnlineTicketBooking")
                .whereEqualTo("ID", bookingId)
                .get()
                .addOnSuccessListener(querySnapshot -> processBookingData(querySnapshot))
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error fetching booking details!", Toast.LENGTH_SHORT).show()
                );
    }

    // Process Firestore query results
    private void processBookingData(QuerySnapshot querySnapshot) {
        if (!querySnapshot.isEmpty()) {
            DocumentSnapshot documentSnapshot = querySnapshot.getDocuments().get(0);

            // Fetch document references
            DocumentReference busRef = documentSnapshot.getDocumentReference("Bus_id");
            DocumentReference fromRef = documentSnapshot.getDocumentReference("From");
            DocumentReference toRef = documentSnapshot.getDocumentReference("To");
            DocumentReference discountRef = documentSnapshot.getDocumentReference("Discount_id");

            // Other details
            String name = documentSnapshot.getString("Name");
            String gender = documentSnapshot.getString("Gender");
            String status = documentSnapshot.getString("Status");
            String dob = documentSnapshot.getString("Date_Of_Birth");
            String email = documentSnapshot.getString("Email");

            // Handle Contact Number safely
            long contact = documentSnapshot.contains("Contact_No") ? documentSnapshot.getLong("Contact_No") : 0;

            // Handle Seats_Number safely
            int seats = documentSnapshot.contains("Seats_Number") ? documentSnapshot.getLong("Seats_Number").intValue() : 0;

            // Fetch related data (Bus, From, To, Discount)
            fetchRelatedDetails(busRef, fromRef, toRef, discountRef, name, contact, gender, dob, email, status, seats);
        } else {
            Toast.makeText(getContext(), "No Booking Found!", Toast.LENGTH_SHORT).show();
        }
    }

    // Fetch related details from Firestore
    private void fetchRelatedDetails(DocumentReference busRef, DocumentReference fromRef, DocumentReference toRef,
                                     DocumentReference discountRef, String name, long contact, String gender,
                                     String dob, String email, String status, int seats) {

        if (busRef != null) {
            busRef.get().addOnSuccessListener(busDoc -> {
                String busName = busDoc.exists() ? busDoc.getString("BusName") : "Unknown Bus";
                int busFare = busDoc.contains("BusFare_Fee") ? busDoc.getLong("BusFare_Fee").intValue() : 0;
                String busType = busDoc.getString("BusType");
                String busStatus = busDoc.getString("Status");

                // Fetch 'From' Station Name
                fromRef.get().addOnSuccessListener(fromDoc -> {
                    String fromStation = fromDoc.exists() ? fromDoc.getString("Name") : "Unknown Station";

                    // Fetch 'To' Station Name
                    toRef.get().addOnSuccessListener(toDoc -> {
                        String toStation = toDoc.exists() ? toDoc.getString("Name") : "Unknown Station";

                        // Fetch Discount Details
                        fetchDiscountDetails(discountRef, name, contact, gender, dob, email, status, seats, busName,
                                busFare, busType, busStatus, fromStation, toStation);
                    });
                });
            });
        }
    }

    // Fetch Discount Details
    private void fetchDiscountDetails(DocumentReference discountRef, String name, long contact, String gender,
                                      String dob, String email, String status, int seats, String busName,
                                      int busFare, String busType, String busStatus, String fromStation,
                                      String toStation) {
        if (discountRef != null) {
            discountRef.get().addOnSuccessListener(discountDoc -> {
                String passType = discountDoc.exists() ? discountDoc.getString("Pass_type") : "No Discount";
                double discountPercentage = discountDoc.contains("Percentage") ? discountDoc.getDouble("Percentage") : 0.0;
                String discountStatus = discountDoc.getString("Status");

                // Calculate Final Price after Discount
                double finalPrice = busFare - (busFare * (discountPercentage / 100));

                // Update TextView with all details
                updateTextView(name, contact, gender, dob, email, status, seats, busName, busFare, busType,
                        busStatus, fromStation, toStation, passType, discountPercentage, discountStatus, finalPrice);
            });
        } else {
            // If no discount, update UI without discount details
            updateTextView(name, contact, gender, dob, email, status, seats, busName, busFare, busType,
                    busStatus, fromStation, toStation, "No Discount", 0.0, "Inactive", busFare);
        }
    }

    // Method to update the TextView
    private void updateTextView(String name, long contact, String gender, String dob, String email, String status,
                                int seats, String busName, int busFare, String busType, String busStatus,
                                String fromStation, String toStation, String passType, double discountPercentage,
                                String discountStatus, double finalPrice) {
        String bookingDetails = "Passenger: " + name + "\n" +
                "Gender: " + gender + "\n" +
                "DOB: " + dob + "\n" +
                "Email: " + email + "\n" +
                "Contact: " + contact + "\n\n\n"  +
                "--- BUS DETAILS ---\n" +
                "Bus: " + busName + "\n" +
                "Type: " + busType + "\n" +
                "Status: " + busStatus + "\n\n\n" +
                "--- TRAVEL DETAILS ---\n" +
                "From: " + fromStation + "\n" +
                "To: " + toStation + "\n" +
                "Seats: " + seats + "\n\n" ;

        txtBookingDetails.setText(bookingDetails);
    }

}