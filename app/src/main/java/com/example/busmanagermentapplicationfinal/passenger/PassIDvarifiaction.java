package com.example.busmanagermentapplicationfinal.passenger;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.busmanagermentapplicationfinal.R;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class PassIDvarifiaction extends AppCompatActivity {

    private static final int PICK_PASSPORT_PHOTO = 1;
    private static final int PICK_ID_PROOF = 2;

    private EditText etIdNumber, etInstitution;
    private ImageView imgPassport;
    private LinearLayout layoutIdProofPreview;

    private Uri passportUri, idProofUri;
    private StorageReference storageRef;
    private FirebaseFirestore db;

    private String passrequestId;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pass_idvarifiaction);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etIdNumber = findViewById(R.id.et_id_number);
        etInstitution = findViewById(R.id.et_institution);
        imgPassport = findViewById(R.id.img_passport);
        layoutIdProofPreview = findViewById(R.id.layout_id_proof_preview);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        db = FirebaseFirestore.getInstance();



        CircularProgressIndicator progressCircle = findViewById(R.id.progressCircle);
        TextView tvStepText = findViewById(R.id.tvStepText);

        // Set progress for "step 3 of 4"
        int currentStep = 3;
        int totalSteps = 4;

        int progressPercent = (int) ((currentStep / (float) totalSteps) * 100);
        progressCircle.setProgress(progressPercent);
        tvStepText.setText(currentStep + " of " + totalSteps);


        passrequestId = getIntent().getStringExtra("passrequestId");

        findViewById(R.id.btn_select_passport).setOnClickListener(v -> pickFile(PICK_PASSPORT_PHOTO));
        findViewById(R.id.btn_select_id_proof).setOnClickListener(v -> pickFile(PICK_ID_PROOF));
        findViewById(R.id.btn_submit).setOnClickListener(v -> validateAndUpload());
    }

    private void pickFile(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        if (requestCode == PICK_ID_PROOF)
            intent.setType("*/*");
        else
            intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select File"), requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (requestCode == PICK_PASSPORT_PHOTO) {
                passportUri = uri;
                imgPassport.setImageURI(uri);
            } else if (requestCode == PICK_ID_PROOF) {
                idProofUri = uri;
                previewIdProof(uri);
            }
        }
    }

    private void previewIdProof(Uri uri) {
        layoutIdProofPreview.removeAllViews();

        String type = getContentResolver().getType(uri);
        View preview;
        if (type != null && type.startsWith("image/")) {
            ImageView image = new ImageView(this);
            image.setImageURI(uri);
            image.setLayoutParams(new LinearLayout.LayoutParams(300, 300));
            preview = image;
        } else {
            TextView pdfText = new TextView(this);
            pdfText.setText("PDF File: " + uri.getLastPathSegment());
            preview = pdfText;
        }

        Button delete = new Button(this);
        delete.setText("Delete");
        delete.setOnClickListener(v -> {
            idProofUri = null;
            layoutIdProofPreview.removeAllViews();
        });

        layoutIdProofPreview.addView(preview);
        layoutIdProofPreview.addView(delete);
    }

    private void validateAndUpload() {
        String idNum = etIdNumber.getText().toString().trim();
        String institution = etInstitution.getText().toString().trim();

        if (idNum.isEmpty() || institution.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (passportUri == null || idProofUri == null) {
            Toast.makeText(this, "Please select required files", Toast.LENGTH_SHORT).show();
            return;
        }

        uploadFiles(idNum, institution);
    }

    private void uploadFiles(String idNum, String institution) {
        String passportPath = "passport_photos/" + System.currentTimeMillis() + ".jpg";
        String idProofPath = "id_proofs/" + System.currentTimeMillis();

        StorageReference passportRef = storageRef.child(passportPath);
        StorageReference idProofRef = storageRef.child(idProofPath);

        passportRef.putFile(passportUri).addOnSuccessListener(taskSnapshot ->
                passportRef.getDownloadUrl().addOnSuccessListener(passportDownloadUrl -> {

                    idProofRef.putFile(idProofUri).addOnSuccessListener(taskSnapshot2 ->
                            idProofRef.getDownloadUrl().addOnSuccessListener(idProofDownloadUrl -> {

                                Map<String, Object> fileMap = new HashMap<>();
                                fileMap.put("PassportPhotoURL", passportDownloadUrl.toString());
                                fileMap.put("IDProofURL", idProofDownloadUrl.toString());
                                fileMap.put("IdNumber", idNum);
                                fileMap.put("Institution", institution);

                                db.collection("PassRequest").document(passrequestId)
                                        .update(fileMap)
                                        .addOnSuccessListener(unused -> {
                                            Toast.makeText(this, "Details uploaded", Toast.LENGTH_SHORT).show();

                                            // TODO: Navigate to next step if required
                                            Intent intent = new Intent(PassIDvarifiaction.this, Pass_PassDetails.class);
                                            intent.putExtra("passrequestId", passrequestId);
                                            startActivity(intent);
                                            finish();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Failed to save data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });

                            })
                    ).addOnFailureListener(e ->
                            Toast.makeText(this, "ID Proof Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());

                })
        ).addOnFailureListener(e ->
                Toast.makeText(this, "Passport Upload Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
