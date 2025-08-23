package com.example.emergency_department_tracking_system;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.DatabaseReference;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ambulance_photo_screen extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    private TextView textViewDateTime, selectPhotoText;
    private ImageView imagePreview;
    private Uri photoUri;
    private File photoFile;

    private String patientId;
    private String currentUid = "driver_uid"; // Replace with actual UID logic

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ambulance_photo_screen);

        selectPhotoText = findViewById(R.id.selectPhoto);
        imagePreview = findViewById(R.id.imagePreview);
        Button buttonSubmit = findViewById(R.id.buttonSubmit);

        // Set current date & time

        // Get patient ID from intent
        patientId = getIntent().getStringExtra("patientId");

        // Camera launch on click
        selectPhotoText.setOnClickListener(v -> openCamera());

        // Submit button logic
        buttonSubmit.setOnClickListener(v -> {
            if (photoUri != null) {
                uploadImageToFirebase();
            } else {
                Toast.makeText(this, "Please capture a photo first", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            try {
                photoFile = createImageFile();

                if (photoFile != null) {
                    photoUri = FileProvider.getUriForFile(
                            this,
                            getPackageName() + ".fileprovider",
                            photoFile
                    );
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                    startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
                } else {
                    Toast.makeText(this, "Error creating image file.", Toast.LENGTH_SHORT).show();
                    Log.e("openCamera", "photoFile is null");
                }

            } catch (Exception e) {
                Toast.makeText(this, "Error opening camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("openCamera", "Error: " + e.getMessage(), e);
            }
        } else {
            Toast.makeText(this, "No camera app found!", Toast.LENGTH_SHORT).show();
            Log.e("openCamera", "Camera app not found");
        }
    }

    private File createImageFile() throws Exception {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && photoUri != null) {
            imagePreview.setVisibility(View.VISIBLE);
            imagePreview.setImageURI(photoUri);
        }
    }

    private void uploadImageToFirebase() {
        StorageReference storageRef = FirebaseStorage.getInstance()
                .getReference("ambulance_photos/" + photoFile.getName());

        UploadTask uploadTask = storageRef.putFile(photoUri);
        uploadTask.addOnSuccessListener(taskSnapshot -> {
            storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                String downloadUrl = uri.toString();

                // Save the URL to Firebase Realtime Database
                DatabaseReference dbRef = FirebaseDatabase.getInstance("https://f-ecg-7df04-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("ambulance_driver")
                        .child(currentUid).child(patientId).child("ambulance_image");

                dbRef.setValue(downloadUrl).addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Image uploaded and URL saved!", Toast.LENGTH_SHORT).show();
                    DatabaseReference sosRefs = FirebaseDatabase.getInstance("https://f-ecg-7df04-default-rtdb.asia-southeast1.firebasedatabase.app/")
                            .getReference("ambulance_driver_status").child(FirebaseAuth.getInstance().getCurrentUser().getUid());


                    sosRefs.child("status").setValue("completed");
                    sosRefs.child("patientid").setValue(patientId);

                    Intent intent=new Intent(ambulance_photo_screen.this,PendingSOSListActivity.class);
                    startActivity(intent);
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });

            });
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
