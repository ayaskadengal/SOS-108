package com.example.emergency_department_tracking_system;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

public class pdf_download extends AppCompatActivity {

    private static final int STORAGE_PERMISSION_CODE = 100;
    private StringBuilder pdfContent = new StringBuilder();
    private Button btnDownload;
String patientId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pdf_download);
        patientId = getIntent().getStringExtra("patientId");

        btnDownload = findViewById(R.id.btnDownload);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            checkStoragePermission();
        } else {
            fetchDataFromFirebase();
        }

        btnDownload.setOnClickListener(view -> fetchDataFromFirebase());
    }

    private void checkStoragePermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE);
        } else {
            fetchDataFromFirebase();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchDataFromFirebase();
        } else {
            Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchDataFromFirebase() {
        DatabaseReference reference = FirebaseDatabase.getInstance("https://f-ecg-7df04-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("emtinformation").child(patientId);

        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Map<String, Object> data = (Map<String, Object>) snapshot.getValue();

                    // Text fields
                    appendLine("Patient ID", getValue(data, "patientID"));
                    appendLine("Patient Name", getValue(data, "name"));
                    appendLine("Age", getValue(data, "age"));
                    appendLine("Gender", getValue(data, "gender"));
                    appendLine("Mobile", getValue(data, "mobileNumber"));
                    appendLine("Location", getValue(data, "locationLink"));
                    appendLine("Chief Complaint", getValue(data, "complaint"));
                    appendLine("Informer", getValue(data, "informer"));

                    pdfContent.append("\n--- Onsite Care ---\n");
                    appendIfTrue(data, "care_checkResponse", "Check Response");
                    appendIfTrue(data, "care_immobilization", "Immobilization");
                    appendIfTrue(data, "care_transport", "Proper Transport");
                    appendIfTrue(data, "care_abc", "ABC");
                    appendIfTrue(data, "care_firstAid", "First Aid");
                    appendIfTrue(data, "care_otherABC", "Other (ABC)");

                    pdfContent.append("\n--- Enroute Care ---\n");
                    appendIfTrue(data, "enroute_airwayManagement", "Airway Management");
                    appendIfTrue(data, "enroute_painSupport", "Pain Support");
                    appendIfTrue(data, "enroute_rapidTransport", "Rapid Transport");
                    appendIfTrue(data, "enroute_monitoring", "Monitoring");
                    appendIfTrue(data, "enroute_hydration", "Hydration");
                    appendIfTrue(data, "enroute_otherMonitoring", "Other (Monitoring)");

                    pdfContent.append("\n--- Drugs Administered ---\n");
                    appendIfTrue(data, "drug_adrenaline", "Adrenaline");
                    appendIfTrue(data, "drug_atorvastatin", "Atorvastatin");
                    appendIfTrue(data, "drug_clopidogrel", "Clopidogrel");
                    appendIfTrue(data, "drug_tranexemicAcid", "Tranexemic Acid");
                    appendIfTrue(data, "drug_ivFluids", "IV Fluids");
                    appendIfTrue(data, "drug_aspirin", "Aspirin");
                    appendIfTrue(data, "drug_atropine", "Atropine");
                    appendIfTrue(data, "drug_diazepam", "Diazepam");
                    appendIfTrue(data, "drug_glucose", "Glucose");
                    appendIfTrue(data, "drug_labetolol", "Labetolol");
                    appendIfTrue(data, "drug_oxygen", "Oxygen");
                    appendIfTrue(data, "drug_salbutamol", "Salbutamol");
                    appendIfTrue(data, "drug_hydrocortisone", "Hydrocortisone");
                    appendIfTrue(data, "drug_otherDrugsExtra", "Other Drugs");
                    generatePDFAndSave();
                } else {
                    Toast.makeText(pdf_download.this, "Patient data not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(pdf_download.this, "Firebase error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void appendLine(String label, String value) {
        pdfContent.append(label).append(": ").append(value).append("\n");
    }

    private void appendIfTrue(Map<String, Object> data, String key, String label) {
        Object val = data.get(key);
        if (val instanceof Boolean && (Boolean) val) {
            pdfContent.append("- ").append(label).append("\n");
        }
    }

    private String getValue(Map<String, Object> data, String key) {
        Object val = data.get(key);
        return val != null ? val.toString() : "N/A";
    }

    private void generatePDFAndSave() {
        PdfDocument pdfDocument = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
        PdfDocument.Page page = pdfDocument.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setTextSize(14f);

        String[] lines = pdfContent.toString().split("\n");
        int x = 40, y = 60;
        for (String line : lines) {
            canvas.drawText(line, x, y, paint);
            y += 25;
        }

        pdfDocument.finishPage(page);

        File pdfFile = getPdfFile();
        try {
            FileOutputStream fos = new FileOutputStream(pdfFile);
            pdfDocument.writeTo(fos);
            fos.close();

            Toast.makeText(this, "PDF saved to " + pdfFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
            openPdf(pdfFile);
        } catch (IOException e) {
            Toast.makeText(this, "Error saving PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        Log.d("error,","rerer"+e.getMessage());
        }

        pdfDocument.close();
    }

    private File getPdfFile() {
        // Create a unique filename using a timestamp or other unique identifier
        String timestamp = String.valueOf(System.currentTimeMillis());
        File docsFolder = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), "EMTForms");

        if (!docsFolder.exists()) {
            docsFolder.mkdirs();
        }

        File pdfFile = new File(docsFolder, "PatientData_" + timestamp + ".pdf");
        return pdfFile;
    }


    private void openPdf(File pdfFile) {
        Uri pdfUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", pdfFile);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(pdfUri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "No PDF viewer found", Toast.LENGTH_SHORT).show();
        }
    }
}
