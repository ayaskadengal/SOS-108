package com.example.emergency_department_tracking_system;

import static androidx.constraintlayout.motion.utils.Oscillator.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class EMTForm extends AppCompatActivity {

    EditText patientIdInput, patientNameInput, mobileNumberInput, genderInput, ageInput, complaintInput, informerInput;
    Button incidentButton, fetchButton;
    String patientId;
    DatabaseReference databaseReference;
    String status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emtform);

        // Initialize Firebase DB reference
        databaseReference = FirebaseDatabase.getInstance("https://f-ecg-7df04-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("patients");

        // Link UI elements
        patientIdInput = findViewById(R.id.patientIdInput);
        patientNameInput = findViewById(R.id.patientNameInput);
        mobileNumberInput = findViewById(R.id.mobileNumberInput);
        genderInput = findViewById(R.id.genderInput);
        ageInput = findViewById(R.id.ageInput);
        complaintInput = findViewById(R.id.complaintInput);
        informerInput = findViewById(R.id.informerInput);
        incidentButton = findViewById(R.id.incidentButton);
        fetchButton = findViewById(R.id.fetchButton);

        // Fetch Button logic
        fetchButton.setOnClickListener(v -> {
            patientId = patientIdInput.getText().toString().trim();
            if (patientId.isEmpty()) {
                Toast.makeText(this, "Enter Patient ID to fetch data", Toast.LENGTH_SHORT).show();
                return;
            }

            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            Log.d("timeingns","sdsd"+ today);

            DatabaseReference sosRef = FirebaseDatabase.getInstance("https://f-ecg-7df04-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("sos")
                    .child(today)
                    .child(patientId);

            sosRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    String status = snapshot.child("status").getValue(String.class);
                    Log.d("rsadsad","sdsd"+ status);
                    if ("taken".equals(status)) {
                        // Status is "taken", now fetch patient data
                        databaseReference.child(patientId).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    String name = snapshot.child("name").getValue(String.class);
                                    String mobile = snapshot.child("number").getValue(String.class);
                                    String gender = snapshot.child("gender").getValue(String.class);
                                    String age = snapshot.child("age").getValue(String.class);
                                    String complaint = snapshot.child("complaint").getValue(String.class);
                                    String informer = snapshot.child("informer").getValue(String.class);

                                    // Populate the form fields with the patient data
                                    patientNameInput.setText(name);
                                    mobileNumberInput.setText(mobile);
                                    genderInput.setText(gender);
                                    ageInput.setText(age);
                                    complaintInput.setText(complaint);
                                    informerInput.setText(informer);

                                    Toast.makeText(EMTForm.this, "Patient data loaded", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(EMTForm.this, "No data found for this Patient ID", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                Toast.makeText(EMTForm.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Toast.makeText(EMTForm.this , "Patient is not taken", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    Log.e(TAG, "Database error: " + error.getMessage());
                }
            });
        });

        // Save/Submit logic
        incidentButton.setOnClickListener(view -> {
            String patientId = patientIdInput.getText().toString().trim();
            if (patientId.isEmpty()) {
                Toast.makeText(EMTForm.this, "Enter Patient ID to proceed", Toast.LENGTH_SHORT).show();
                return;
            }

            // Intent to move to the next activity
            Intent intent = new Intent(this, emt_data_collection.class);
            intent.putExtra("patientId", patientId);  // Pass patientId to the next screen
            startActivity(intent);

            // Save or submit data logic can be added here, if needed
            String name = patientNameInput.getText().toString().trim();
            String mobile = mobileNumberInput.getText().toString().trim();
            String gender = genderInput.getText().toString().trim();
            String age = ageInput.getText().toString().trim();
            String complaint = complaintInput.getText().toString().trim();
            String informer = informerInput.getText().toString().trim();

            // Save to Firebase or do further logic here...
        });
    }
}
