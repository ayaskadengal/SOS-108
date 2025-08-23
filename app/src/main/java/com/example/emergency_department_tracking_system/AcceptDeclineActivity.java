package com.example.emergency_department_tracking_system;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AcceptDeclineActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;
    private String patientId;
    private String patientLat, patientLon;
    private Location currentLocation;
    private EditText editTextDeclineReason;
    private Button buttonSubmitDecline;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView distanceTextView;
    private Button refreshButton;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_decline);

        distanceTextView = findViewById(R.id.textViewDistance);
        refreshButton = findViewById(R.id.buttonRefresh);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Get patient ID from intent
        patientId = getIntent().getStringExtra("patientId");
        if (patientId == null) {
            Toast.makeText(this, "No patient ID provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        DatabaseReference sdd=FirebaseDatabase.getInstance("https://f-ecg-7df04-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("User_Type").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        sdd.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String user_type=snapshot.child("type").getValue(String.class);
                Log.d("type","type" +user_type);
                Log.d("CHECK_USER_TYPE", "user_type = " + user_type + ", equals Ambulance_Driver? " + user_type.equals("Ambulance_Driver"));

                if(user_type.equals("Ambulance_Driver"))
                {
                    requestLocationPermission();
                }
                else
                {

                    startActivity(new Intent(AcceptDeclineActivity.this, EMTForm.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

       // Ask for location permission
        editTextDeclineReason = findViewById(R.id.editTextDeclineReason);
        buttonSubmitDecline = findViewById(R.id.buttonSubmitDecline);
        Button buttonAccept = findViewById(R.id.buttonAccept);
        Button buttonDecline = findViewById(R.id.buttonDecline);

        buttonAccept.setOnClickListener(v -> acceptRequest());
        buttonDecline.setOnClickListener(v -> finish());
        buttonDecline.setOnClickListener(v -> {
            editTextDeclineReason.setVisibility(View.VISIBLE);
            buttonSubmitDecline.setVisibility(View.VISIBLE);
        });
        refreshButton.setOnClickListener(v -> getLocationAndFetchPatientData());
        buttonSubmitDecline.setOnClickListener(v -> {
            String reason = editTextDeclineReason.getText().toString().trim();
            if (reason.isEmpty()) {
                Toast.makeText(this, "Please provide a reason", Toast.LENGTH_SHORT).show();
                return;
            }

            // Save the decline reason to Firebase
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            DatabaseReference sosRef = FirebaseDatabase.getInstance("https://f-ecg-7df04-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("Declined Cases").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .child(today)
                    .child(patientId);

            sosRef.child("decline_reason").setValue(reason);

            // Optional: Update ambulance_driver_status


            Toast.makeText(this, "Decline reason submitted", Toast.LENGTH_SHORT).show();
            finish(); // Close the activity or redirect if needed
        });

    }


    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLocationAndFetchPatientData(); // Already granted
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationAndFetchPatientData();
            } else {
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getLocationAndFetchPatientData() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }

        fusedLocationClient.getCurrentLocation(
                com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        currentLocation = location;
                        fetchPatientLocationFromFirebase();
                    } else {
                        Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void fetchPatientLocationFromFirebase() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        DatabaseReference patientRef = FirebaseDatabase.getInstance("https://f-ecg-7df04-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("patients")
                .child(patientId);

        patientRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    patientLat = snapshot.child("latitude").getValue(String.class);
                    patientLon = snapshot.child("longitude").getValue(String.class);
                    double lati=Double.parseDouble(patientLat);
                    double longi=Double.parseDouble(patientLon);


                    // Check if data is null or invalid
                    if (patientLat == null || patientLon == null) {
                        Toast.makeText(AcceptDeclineActivity.this, "Patient location data is missing", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Logging the values to verify they are correct
                    Log.d("Patient Location", "Latitude: " + patientLat + ", Longitude: " + patientLon);

                    if (currentLocation != null) {
                        double distanceInKm = DistanceCalculator.calculateDistance(
                                currentLocation.getLatitude(), currentLocation.getLongitude(),
                                lati, longi);
                        Log.d("distance in km", "Distance: " + distanceInKm);

                        distanceTextView.setText("Approximate Distance to patient: " + String.format(Locale.US, "%.2f", distanceInKm) + " km");
                    }
                    if (mMap != null) {
                        updateMapMarkers();
                    }
                } catch (Exception e) {
                    Log.e("Error", "Error reading location", e);
                    Toast.makeText(AcceptDeclineActivity.this, "Error reading location", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AcceptDeclineActivity.this, "Failed to get patient location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void acceptRequest() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        DatabaseReference sosRef = FirebaseDatabase.getInstance("https://f-ecg-7df04-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("sos")
                .child(today)
                .child(patientId);

        sosRef.child("status").setValue("taken");
        DatabaseReference sosRefs = FirebaseDatabase.getInstance("https://f-ecg-7df04-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("ambulance_driver_status").child(FirebaseAuth.getInstance().getCurrentUser().getUid());


        sosRefs.child("status").setValue("taken");
        sosRefs.child("patientid").setValue(patientId);
        sosRefs.child("ambulance_id").setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
        sosRef.child("ambulance_id").setValue(FirebaseAuth.getInstance().getCurrentUser().getUid());
        String phoneNumber = "8113019574"; // Replace with recipient number
        String message = "Hello, this is a test message!";

        Intent intents = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", phoneNumber, null));
        intents.putExtra("sms_body", message);
        startActivity(intents);
        Intent intent = new Intent(this, ambulance_driver_home_screen.class);
        intent.putExtra("patientId", patientId);
        startActivity(intent);

        finish();
    }

    @Override


    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        // If data is already available, update map now
        if (currentLocation != null && patientLat != null && patientLon != null) {
            updateMapMarkers();
        }
    }

    private void updateMapMarkers() {
        if (mMap == null || currentLocation == null || patientLat == null || patientLon == null) return;

        mMap.clear();

        LatLng driverLocation = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
        LatLng patientLocation = new LatLng(Double.parseDouble(patientLat), Double.parseDouble(patientLon));

        mMap.addMarker(new MarkerOptions()
                .position(driverLocation)
                .title("Your Location")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        mMap.addMarker(new MarkerOptions()
                .position(patientLocation)
                .title("Patient Location"));

        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(driverLocation)
                .include(patientLocation)
                .build();

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
    }


}
