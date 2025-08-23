package com.example.emergency_department_tracking_system;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ambulance_driver_home_screen extends AppCompatActivity {

    private String patientId;
    private EditText editTextPatientId, editTextMobileNumber, editTextLocation;
    private Button buttonArrived, buttonOpenMaps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ambulance_driver_home_screen);
        Intent serviceIntent = new Intent(this, SOSMonitorService.class);
        startService(serviceIntent);
        // Get patientId from Intent
        patientId = getIntent().getStringExtra("patientId");

        editTextPatientId = findViewById(R.id.editTextPatientId);
        editTextMobileNumber = findViewById(R.id.editTextMobileNumber);
        editTextLocation = findViewById(R.id.editTextLocation);
        buttonArrived = findViewById(R.id.buttonArrived);
        buttonOpenMaps = findViewById(R.id.buttonOpenMaps);

        // Set the patient ID
        if (patientId != null) {
            editTextPatientId.setText(patientId);
            fetchPatientDetails(patientId);
        } else {
            Toast.makeText(this, "Patient ID is missing", Toast.LENGTH_SHORT).show();
        }

        buttonOpenMaps.setOnClickListener(v -> {
            String locationUrl = editTextLocation.getText().toString().trim();

            if (!locationUrl.isEmpty()) {
                String destination;

                // Case 1: Full Google Maps URL
                if (locationUrl.contains("maps/dir/")) {
                    // Extract coordinates after "/dir/"
                    int index = locationUrl.indexOf("maps/dir/") + 9;
                    destination = locationUrl.substring(index);
                }
                // Case 2: Just coordinates (e.g., "13.0836939,80.2701860")
                else if (locationUrl.matches("^-?\\d+(\\.\\d+)?,-?\\d+(\\.\\d+)?$")) {
                    destination = locationUrl;
                }
                else {
                    Toast.makeText(this, "Invalid location format", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create navigation intent
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + Uri.encode(destination));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");

                // Start navigation
                try {
                    startActivity(mapIntent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, "Google Maps app not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Location URL is empty", Toast.LENGTH_SHORT).show();
            }
        });


        buttonArrived.setOnClickListener(v -> {
            Toast.makeText(this, "Marked as Arrived!", Toast.LENGTH_SHORT).show();
            Intent intent=new Intent(this,ambulance_photo_screen.class);
            intent.putExtra("patientId", patientId);
            startActivity(intent);
            // You can update Firebase or navigate further as needed
        });
    }

    private void fetchPatientDetails(String patientId) {
        DatabaseReference patientRef = FirebaseDatabase.getInstance("https://f-ecg-7df04-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("patients").child(patientId);

        patientRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String mobile = snapshot.child("number").getValue(String.class);
                String location = snapshot.child("locationLink").getValue(String.class);

                if (mobile != null) editTextMobileNumber.setText(mobile);
                if (location != null) editTextLocation.setText(location);
                editTextMobileNumber.setFocusable(false);
                editTextMobileNumber.setClickable(true);
                editTextMobileNumber.setOnClickListener(v -> {
                    String mobiles = editTextMobileNumber.getText().toString().trim();
                    if (!mobiles.isEmpty()) {
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse("tel:" + mobiles));
                        startActivity(intent);
                    } else {
                        Toast.makeText(ambulance_driver_home_screen.this, "Mobile number is empty", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ambulance_driver_home_screen.this, "Failed to fetch patient data", Toast.LENGTH_SHORT).show();
                Log.e("FirebaseError", error.getMessage());
            }
        });
    }
}
