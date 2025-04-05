package com.example.busmanagementapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the  factory method to
 * create an instance of this fragment.
 */
public class Conductor_BookTIcketFragment extends Fragment {
    AutoCompleteTextView txtBus,autoCompleteSource, autoCompleteDestination;
    EditText edtContactNumber,txtAdult,txtSenior,txtChild;
//    Spinner spinnerPassType;
    Button submitButton;
    FirebaseFirestore db;
    private DatabaseReference databaseReference;
    List<String> busList;
    List<String> passTypeList;
    List<String> stationList;
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_conductor__book_t_icket, container, false);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        txtBus = view.findViewById(R.id.txtBus);
        autoCompleteSource = view.findViewById(R.id.txtSource);
        autoCompleteDestination = view.findViewById(R.id.txtDestination);
        edtContactNumber = view.findViewById(R.id.edtContactNumber);
        txtChild=view.findViewById(R.id.txtChild);
        txtAdult=view.findViewById(R.id.txtAdult);
        txtSenior=view.findViewById(R.id.txtSenior);

        submitButton = view.findViewById(R.id.loginButton);
        busList = new ArrayList<>();
        passTypeList = new ArrayList<>();
        stationList = new ArrayList<>();

        // Fetch bus names from Firestore
        fetchBusNames();
//        fetchPassTypes();
        fetchStationNames();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateAndSubmit();
            }
        });
        return view;
    }

    private void fetchBusNames() {
        CollectionReference busRef = db.collection("Bus_Details");

        busRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String busName = document.getString("BusName"); // Assuming 'Name' is the field
                    if (busName != null) {
                        busList.add(busName);
                    }
                }
                // Set data in AutoCompleteTextView
                ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.custom_dropdown_item_autotextview, busList);
                txtBus.setAdapter(adapter);
                txtBus.setThreshold(1);
            } else {
                Toast.makeText(requireContext(), "Error fetching buses", Toast.LENGTH_SHORT).show();
            }
        });
    }

//    private void fetchPassTypes()
//    {
//        db.collection("Discount")
//                .get()
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        passTypeList = new ArrayList<>();
//                        passTypeList.add("Select Pass Type"); // Default option
//                        for (QueryDocumentSnapshot document : task.getResult()) {
//                            String passType = document.getString("Pass_type");
//                            if (passType != null) {
//                                passTypeList.add(passType);
//                            }
//                        }
//
//                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
//                                android.R.layout.simple_list_item_1, passTypeList);
//
//
//                        spinnerPassType.setAdapter(adapter);
//                    } else {
//                        Toast.makeText(requireContext(), "Error fetching pass types", Toast.LENGTH_SHORT).show();
//                    }
//                });
//    }

    private void fetchStationNames() {
        db.collection("Station")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    stationList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String stationName = document.getString("Name"); // Assuming "Name" is the field
                        if (stationName != null) {
                            stationList.add(stationName);
                        }
                    }

                    // Set adapter after fetching
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.custom_dropdown_item_autotextview, stationList);


                    autoCompleteSource.setAdapter(adapter);
                    autoCompleteDestination.setAdapter(adapter);
                    autoCompleteSource.setThreshold(1);
                    autoCompleteDestination.setThreshold(1);
                })
                .addOnFailureListener(e -> Toast.makeText(requireContext(), "Error fetching stations", Toast.LENGTH_SHORT).show());

    }

    private void validateAndSubmit() {
        String busName = txtBus.getText().toString().trim();
        String source = autoCompleteSource.getText().toString().trim();
        String destination = autoCompleteDestination.getText().toString().trim();
        String contactNumber = edtContactNumber.getText().toString().trim();
        String childSeatsStr = txtChild.getText().toString().trim();
        String adultSeatsStr = txtAdult.getText().toString().trim();
        String seniorSeatsStr = txtSenior.getText().toString().trim();

        // Convert seats to integer (default to 0 if empty)
        int childSeats = TextUtils.isEmpty(childSeatsStr) ? 0 : Integer.parseInt(childSeatsStr);
        int adultSeats = TextUtils.isEmpty(adultSeatsStr) ? 0 : Integer.parseInt(adultSeatsStr);
        int seniorSeats = TextUtils.isEmpty(seniorSeatsStr) ? 0 : Integer.parseInt(seniorSeatsStr);

        // 1. Check if Bus exists
        if (!busList.contains(busName)) {
            txtBus.setError("Bus not found in the list!");
            return;
        }

        // 2. Check if at least one seat is entered
        if (childSeats + adultSeats + seniorSeats == 0) {
            Toast.makeText(getContext(), "Please enter at least one seat!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. Check if Source & Destination exist in the station list
        if (!stationList.contains(source)) {
            autoCompleteSource.setError("Source Station not found!");
            return;
        }

        if (!stationList.contains(destination)) {
            autoCompleteDestination.setError("Destination Station not found!");
            return;
        }

        // 4. Ensure Source & Destination are not the same
        if (source.equals(destination)) {
            Toast.makeText(getContext(), "Source and Destination cannot be the same!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 5. Ensure Contact Number is filled
        if (TextUtils.isEmpty(contactNumber) || contactNumber.length() < 10) {
            edtContactNumber.setError("Enter a valid Contact Number!");
            return;
        }

        // 6. Store Data in Firebase
        int totalSeats = childSeats + adultSeats + seniorSeats;
        storeDataInFirebase(busName, source, destination, contactNumber, totalSeats, seniorSeats);
    }

    private void storeDataInFirebase(String bus, String source, String destination, String contact, int totalSeats, int seniorSeats) {
        getDocumentId("Bus_Details", "BusName", bus, busRef -> {
            getDocumentId("Station", "Name", source, sourceRef -> {
                getDocumentId("Station", "Name", destination, destRef -> {

                        // Define seat price and senior discount
                        int seatPrice = 100;
                        double seniorDiscount = 0.5; // 50% discount for seniors

                        // Calculate total amount with discount for seniors
                        int totalAmount = ((totalSeats - seniorSeats) * seatPrice) + ((int) (seniorSeats * seatPrice * (1 - seniorDiscount)));

                        // Create ticket data
                        Map<String, Object> ticketData = new HashMap<>();
                        ticketData.put("Bus_id", busRef);
                        ticketData.put("From", sourceRef);
                        ticketData.put("To", destRef);
//                        ticketData.put("Discount_id", discountRef);
                        ticketData.put("ContactNo", contact);
                        ticketData.put("TotalSeats", totalSeats);

                        // Store data in Firestore
                        db.collection("OfflineTicketBooking")
                                .add(ticketData)
                                .addOnSuccessListener(documentReference -> {
                                    Log.d("Firestore", "Ticket stored with ID: " + documentReference.getId());

                                    // Show the print dialog with totalAmount
                                    showPrintDialog(totalAmount);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("Firestore", "Error storing ticket", e);
                                    Toast.makeText(getContext(), "Booking Failed!", Toast.LENGTH_SHORT).show();
                                });

                    });
                });
            });
//        });
    }


    // Function to get Firestore document ID by field value
    private void getDocumentId(String collection, String fieldName, String value, OnSuccessListener<DocumentReference> onSuccess) {
        db.collection(collection)
                .whereEqualTo(fieldName, value)  // Match field with user input
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot doc = queryDocumentSnapshots.getDocuments().get(0);
                        DocumentReference docRef = db.collection(collection).document(doc.getId());
                        onSuccess.onSuccess(docRef);
                    } else {
                        Toast.makeText(getContext(), "No match found in " + collection, Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Error fetching " + collection + " ID", e);
                });
    }

    private void showPrintDialog(int totalAmount) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Booking Successful");
        builder.setMessage("Total Amount: ₹" + totalAmount + "\n\n  ");

        builder.setPositiveButton("Ok", (dialog, which) -> {
            txtBus.setText("");  // Reset bus field
            txtChild.setText("");
            txtAdult.setText("");
            txtSenior.setText("");
            autoCompleteSource.setText("");  // Reset source station
            autoCompleteDestination.setText("");  // Reset destination station
            edtContactNumber.setText("");  // Reset contact number
        });



        AlertDialog dialog = builder.create();
        dialog.show();
    }

}
