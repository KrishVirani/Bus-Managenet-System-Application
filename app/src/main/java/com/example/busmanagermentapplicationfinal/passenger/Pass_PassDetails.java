package com.example.busmanagermentapplicationfinal.passenger;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.busmanagermentapplicationfinal.R;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Pass_PassDetails extends AppCompatActivity {
    private Spinner durationSpinner;
    private RadioGroup categoryRadioGroup;
    private AutoCompleteTextView fromStation, toStation;

    private FirebaseFirestore db;
    private Map<String, DocumentReference> stationMap = new HashMap<>();

    @Override
    @SuppressLint({"MissingInflatedId", "LocalSuppress"})
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pass_pass_details);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        CircularProgressIndicator progressCircle = findViewById(R.id.progressCircle);
        TextView tvStepText = findViewById(R.id.tvStepText);

        // Set progress for "step 3 of 4"
        int currentStep = 4;
        int totalSteps = 4;

        int progressPercent = (int) ((currentStep / (float) totalSteps) * 100);
        progressCircle.setProgress(progressPercent);
        tvStepText.setText(currentStep + " of " + totalSteps);


        db = FirebaseFirestore.getInstance();

        durationSpinner = findViewById(R.id.spinner_duration);
        categoryRadioGroup = findViewById(R.id.radio_group_category);
        fromStation = findViewById(R.id.auto_from_station);
        toStation = findViewById(R.id.auto_to_station);

        setupDurationSpinner();
        loadStations();

        String passRequestId = getIntent().getStringExtra("passrequestId");

        Button btnSubmit = findViewById(R.id.btn_submit); // Add this button in XML too
        btnSubmit.setOnClickListener(v -> {
            String duration = durationSpinner.getSelectedItem().toString();
            String category = getSelectedCategory();
            String from = fromStation.getText().toString();
            String to = toStation.getText().toString();
            DocumentReference fromRef = stationMap.get(from);
            DocumentReference toRef = stationMap.get(to);
            Toast.makeText(this, "fromRef: " + fromRef.getId(), Toast.LENGTH_SHORT).show();
            if (from.isEmpty() || to.isEmpty() || category.isEmpty()) {
                Toast.makeText(this, "Please fill all details", Toast.LENGTH_SHORT).show();
                return;
            }

            Map<String, Object> passDetails = new HashMap<>();
            passDetails.put("Duration", duration);
            passDetails.put("Category", category);
            passDetails.put("FromStation", fromRef);
            passDetails.put("ToStation", toRef);

            db.collection("PassRequest").document(passRequestId)
                    .update(passDetails)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Pass details saved successfully!", Toast.LENGTH_SHORT).show();
                        // You can move to next screen here
                        Intent intent = new Intent(Pass_PassDetails.this, PassPreview.class);
                        intent.putExtra("passrequestId", passRequestId);
                        startActivity(intent);
                        finish();

                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error saving pass details.", Toast.LENGTH_SHORT).show();
                    });
        });
    }

    private void setupDurationSpinner() {
        String[] durations = {"Monthly", "Quarterly", "Yearly"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, durations);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        durationSpinner.setAdapter(adapter);
    }

    private void loadStations() {
        db.collection("Station")
                .whereEqualTo("Status", "Active")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> stationNames = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String name = doc.getString("Name");
                        stationNames.add(name);
                        stationMap.put(name, doc.getReference());
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, stationNames);
                    fromStation.setAdapter(adapter);
                    toStation.setAdapter(adapter);
                    fromStation.setThreshold(1);
                    toStation.setThreshold(1);
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load stations", Toast.LENGTH_SHORT).show());
    }

    // Optional: Get selected category value
    private String getSelectedCategory() {
        int checkedId = categoryRadioGroup.getCheckedRadioButtonId();
        if (checkedId != -1) {
            RadioButton selected = findViewById(checkedId);
            return selected.getText().toString();
        }
        return "";
    }
}