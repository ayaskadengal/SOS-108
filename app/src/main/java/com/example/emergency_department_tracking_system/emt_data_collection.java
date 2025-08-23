package com.example.emergency_department_tracking_system;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class emt_data_collection extends AppCompatActivity {

    EditText editTextPatientId, editTextName, editTextAge, editTextMobile, editTextLocation,
            editTextComplaint, editTextOtherDrugs;
    RadioGroup radioGroupGender, radioGroupInformer;

    // Care
    CheckBox checkResponse, immobilization, transport, abc, firstAid, otherABC;

    // Enroute
    CheckBox airwayManagement, painSupport, rapidTransport, monitoring, hydration, otherMonitoring;

    // Drugs
    CheckBox adrenaline, atorvastatin, clopidogrel, tranexemicAcid, ivFluids, aspirin, atropine,
            diazepam, glucose, labetolol, oxygen, salbutamol, hydrocortisone, otherDrugsExtra;

    Button btnSubmit, btnGeneratePdf;
    String patientId;
    DatabaseReference dbRef = FirebaseDatabase.getInstance("https://f-ecg-7df04-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("emtinformation");
    DatabaseReference dbRefs = FirebaseDatabase.getInstance("https://f-ecg-7df04-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("patients");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emt_data_collection);

        // EditTexts
        editTextPatientId = findViewById(R.id.editTextPatientId);
        editTextName = findViewById(R.id.editTextName);
        editTextAge = findViewById(R.id.editTextAge);
        editTextMobile = findViewById(R.id.editTextMobile);
        editTextLocation = findViewById(R.id.editTextLocation);
        editTextComplaint = findViewById(R.id.editTextComplaint);
        editTextOtherDrugs = findViewById(R.id.editTextOtherDrugs);

        // RadioGroups
        radioGroupGender = findViewById(R.id.radioGroupGender);
        radioGroupInformer = findViewById(R.id.radioGroupInformer);

        // Emergency Care Checkboxes
        checkResponse = findViewById(R.id.checkbox_check_response);
        immobilization = findViewById(R.id.checkbox_immobilization);
        transport = findViewById(R.id.checkbox_transport);
        abc = findViewById(R.id.checkbox_abc);
        firstAid = findViewById(R.id.checkbox_first_aid);
        otherABC = findViewById(R.id.checkbox_other_abc);

        // Enroute Care Checkboxes
        airwayManagement = findViewById(R.id.checkbox_airway);
        painSupport = findViewById(R.id.checkbox_pain_support);
        rapidTransport = findViewById(R.id.checkbox_rapid_transport);
        monitoring = findViewById(R.id.checkbox_monitoring);
        hydration = findViewById(R.id.checkbox_hydration);
        otherMonitoring = findViewById(R.id.checkbox_other_monitoring);

        // Drug Checkboxes
        adrenaline = findViewById(R.id.checkbox_adrenaline);
        atorvastatin = findViewById(R.id.checkbox_atorvastatin);
        clopidogrel = findViewById(R.id.checkbox_clopidogrel);
        tranexemicAcid = findViewById(R.id.checkbox_tranexemic);
        ivFluids = findViewById(R.id.checkbox_ivfluids);
        aspirin = findViewById(R.id.checkbox_aspirin);
        atropine = findViewById(R.id.checkbox_atropine);
        diazepam = findViewById(R.id.checkbox_diazepam);
        glucose = findViewById(R.id.checkbox_glucose);
        labetolol = findViewById(R.id.checkbox_labetolol);
        oxygen = findViewById(R.id.checkbox_oxygen);
        salbutamol = findViewById(R.id.checkbox_salbutamol);
        hydrocortisone = findViewById(R.id.checkbox_hydrocortisone);
        otherDrugsExtra = findViewById(R.id.checkbox_other_drugs_extra);

        // Buttons
        btnSubmit = findViewById(R.id.btnSubmit);
        btnGeneratePdf = findViewById(R.id.btnGeneratePdf);

        fetchPatientData();

        btnSubmit.setOnClickListener(v -> saveFormDataToFirebase());
        btnGeneratePdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(emt_data_collection.this,pdf_download.class);
                intent.putExtra("patientId", patientId);
                startActivity(intent);
            }
        });
    }

    private void fetchPatientData() {
       patientId = getIntent().getStringExtra("patientId");
        editTextPatientId.setText(patientId);
        if (TextUtils.isEmpty(patientId)) return;

        dbRefs.child(patientId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    editTextName.setText(snapshot.child("name").getValue(String.class));
                    editTextAge.setText(snapshot.child("age").getValue(String.class));
                    editTextMobile.setText(snapshot.child("number").getValue(String.class));
                    editTextLocation.setText(snapshot.child("locationLink").getValue(String.class));
                    editTextComplaint.setText(snapshot.child("complaint").getValue(String.class));
                    checkRadioByValue(radioGroupGender, snapshot.child("gender").getValue(String.class));
                    checkRadioByValue(radioGroupInformer, snapshot.child("informer").getValue(String.class));
                } else {
                    Toast.makeText(emt_data_collection.this, "No data found for ID: " + patientId, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(emt_data_collection.this, "Database error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveFormDataToFirebase() {
        String patientId = editTextPatientId.getText().toString().trim();
        if (TextUtils.isEmpty(patientId)) {
            Toast.makeText(this, "Patient ID is required", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("patientID",editTextPatientId.getText().toString().trim());
        data.put("name", editTextName.getText().toString().trim());
        data.put("age", editTextAge.getText().toString().trim());
        data.put("mobileNumber", editTextMobile.getText().toString().trim());
        data.put("locationLink", editTextLocation.getText().toString().trim());
        data.put("complaint", editTextComplaint.getText().toString().trim());
        data.put("gender", getSelectedRadioValue(radioGroupGender));
        data.put("informer", getSelectedRadioValue(radioGroupInformer));
        data.put("otherDrugs", editTextOtherDrugs.getText().toString().trim());

        // Care
        data.put("care_checkResponse", checkResponse.isChecked());
        data.put("care_immobilization", immobilization.isChecked());
        data.put("care_transport", transport.isChecked());
        data.put("care_abc", abc.isChecked());
        data.put("care_firstAid", firstAid.isChecked());
        data.put("care_otherABC", otherABC.isChecked());

        // Enroute
        data.put("enroute_airwayManagement", airwayManagement.isChecked());
        data.put("enroute_painSupport", painSupport.isChecked());
        data.put("enroute_rapidTransport", rapidTransport.isChecked());
        data.put("enroute_monitoring", monitoring.isChecked());
        data.put("enroute_hydration", hydration.isChecked());
        data.put("enroute_otherMonitoring", otherMonitoring.isChecked());

        // Drugs
        data.put("drug_adrenaline", adrenaline.isChecked());
        data.put("drug_atorvastatin", atorvastatin.isChecked());
        data.put("drug_clopidogrel", clopidogrel.isChecked());
        data.put("drug_tranexemicAcid", tranexemicAcid.isChecked());
        data.put("drug_ivFluids", ivFluids.isChecked());
        data.put("drug_aspirin", aspirin.isChecked());
        data.put("drug_atropine", atropine.isChecked());
        data.put("drug_diazepam", diazepam.isChecked());
        data.put("drug_glucose", glucose.isChecked());
        data.put("drug_labetolol", labetolol.isChecked());
        data.put("drug_oxygen", oxygen.isChecked());
        data.put("drug_salbutamol", salbutamol.isChecked());
        data.put("drug_hydrocortisone", hydrocortisone.isChecked());
        data.put("drug_otherDrugsExtra", otherDrugsExtra.isChecked());

        dbRef.child(patientId).updateChildren(data).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Patient data saved", Toast.LENGTH_SHORT).show();
           btnGeneratePdf.setVisibility(View.VISIBLE);
                String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                DatabaseReference sosRef = FirebaseDatabase.getInstance("https://f-ecg-7df04-default-rtdb.asia-southeast1.firebasedatabase.app/")
                        .getReference("sos")
                        .child(today)
                        .child(patientId);

                sosRef.child("status").setValue("Completed ");
            } else {
                Toast.makeText(this, "Failed to save data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getSelectedRadioValue(RadioGroup group) {
        int selectedId = group.getCheckedRadioButtonId();
        if (selectedId != -1) {
            RadioButton selectedRadio = findViewById(selectedId);
            return selectedRadio.getText().toString();
        }
        return "";
    }

    private void checkRadioByValue(RadioGroup group, String value) {
        for (int i = 0; i < group.getChildCount(); i++) {
            if (group.getChildAt(i) instanceof RadioButton) {
                RadioButton rb = (RadioButton) group.getChildAt(i);
                if (rb.getText().toString().equalsIgnoreCase(value)) {
                    rb.setChecked(true);
                    break;
                }
            }
        }
    }
}
