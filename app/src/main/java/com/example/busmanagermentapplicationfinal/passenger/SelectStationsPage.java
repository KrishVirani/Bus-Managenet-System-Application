package com.example.busmanagermentapplicationfinal.passenger;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.busmanagermentapplicationfinal.R;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class SelectStationsPage extends AppCompatActivity {
    EditText fromEditText;
    ListView suggestionList;
    List<String> allStations = new ArrayList<>();
    List<String> filteredStations = new ArrayList<>();
    ArrayAdapter<String> adapter;
    FirebaseFirestore db;
    ImageView backButton;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_select_stations_page);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        FirebaseApp.initializeApp(this);
        suggestionList=findViewById(R.id.suggestionList);
        fromEditText=findViewById(R.id.fromEditText);
        backButton = findViewById(R.id.btnbk);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        db = FirebaseFirestore.getInstance();

        adapter = new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, filteredStations);
        suggestionList.setAdapter(adapter);

        // Fetch all stations once
        fetchAllStations();

//        fromEditText.setThreshold(1); // show suggestions after 1 character

        fromEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                adapter.getFilter().filter(s);
                filteredStations(s.toString());
            }
        });

        suggestionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selected = filteredStations.get(position);

                Intent resultIntent = new Intent();
                resultIntent.putExtra("selectedStation", selected);
                setResult(RESULT_OK, resultIntent);
                finish();
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        });
    }

    private void filteredStations(String query) {
        filteredStations.clear();
        if (!query.isEmpty()) {
            for (String station : allStations) {
                if (station.toLowerCase().contains(query.toLowerCase())) {
                    filteredStations.add(station);
                }
            }
            suggestionList.setVisibility(View.VISIBLE);
        } else {
            suggestionList.setVisibility(View.GONE);
        }
        adapter.notifyDataSetChanged();
    }

    private void fetchAllStations() {
        db.collection("Station")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    allStations.clear();
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("Name");
                        if (name != null) {
                            allStations.add(name);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to fetch stations", Toast.LENGTH_SHORT).show();
                });
    }
}