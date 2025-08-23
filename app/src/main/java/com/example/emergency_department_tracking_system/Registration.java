package com.example.emergency_department_tracking_system;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import util.userAPI;

public class Registration extends AppCompatActivity {

    private TextView username, password, name, confPass, mobile_number, emergency_mobile_number2;
    private Spinner sp_gender;
    private Button create_acc;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;
    private final String DB_URL = "https://f-ecg-7df04-default-rtdb.asia-southeast1.firebasedatabase.app";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        mAuth = FirebaseAuth.getInstance();

        username = findViewById(R.id.user_name_email);
        password = findViewById(R.id.password_et);
        confPass = findViewById(R.id.confirm_password_et);
        name = findViewById(R.id.full_name_et);
        mobile_number = findViewById(R.id.mobile_et);
        emergency_mobile_number2 = findViewById(R.id.emergency_contact_et);
        sp_gender = findViewById(R.id.gender_select);
        create_acc = findViewById(R.id.create_ac_bt);
        progressBar = findViewById(R.id.progressbar);

        ArrayAdapter<CharSequence> adapter_gender = ArrayAdapter.createFromResource(this, R.array.gender_select, R.layout.spinner_item);
        adapter_gender.setDropDownViewResource(R.layout.spinner_drop_down);
        sp_gender.setAdapter(adapter_gender);

        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().getTime());

        create_acc.setOnClickListener(v -> {
            String email_txt = username.getText().toString().trim();
            String gender_txt = sp_gender.getSelectedItem().toString();
            String fullname = name.getText().toString().trim();
            String password_txt = password.getText().toString().trim();
            String confirmPassword_txt = confPass.getText().toString().trim();
            String mobile_txt = mobile_number.getText().toString().trim();
            String emergencyMobile_txt = emergency_mobile_number2.getText().toString().trim();

            if (TextUtils.isEmpty(fullname)) {
                name.setError("Full name required");
                name.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(gender_txt)) {
                Toast.makeText(Registration.this, "Please select a gender", Toast.LENGTH_SHORT).show();
                sp_gender.requestFocus();
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email_txt).matches()) {
                username.setError("Valid email required");
                username.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(password_txt)) {
                password.setError("Password required");
                password.requestFocus();
                return;
            }
            if (password_txt.length() < 6) {
                password.setError("Minimum 6 characters");
                password.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(confirmPassword_txt)) {
                confPass.setError("Confirm your password");
                confPass.requestFocus();
                return;
            }
            if (!password_txt.equals(confirmPassword_txt)) {
                confPass.setError("Passwords do not match");
                confPass.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(mobile_txt)) {
                mobile_number.setError("Mobile number required");
                mobile_number.requestFocus();
                return;
            }
            if (mobile_txt.length() != 10) {
                mobile_number.setError("Enter a valid 10-digit number");
                mobile_number.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(emergencyMobile_txt)) {
                emergency_mobile_number2.setError("Emergency number required");
                emergency_mobile_number2.requestFocus();
                return;
            }
            if (emergencyMobile_txt.length() != 10) {
                emergency_mobile_number2.setError("Enter a valid 10-digit number");
                emergency_mobile_number2.requestFocus();
                return;
            }

            progressBar.setVisibility(View.VISIBLE);

            if (isNetworkAvailable()) {
                mAuth.createUserWithEmailAndPassword(email_txt, password_txt).addOnCompleteListener(task -> {
                    progressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        FirebaseUser fuser = mAuth.getCurrentUser();
                        if (fuser != null) {
                            fuser.sendEmailVerification().addOnSuccessListener(unused -> {
                                Toast.makeText(Registration.this, "Verification email sent", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(Registration.this, login_page.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                                finish();
                            });

                            String uid = fuser.getUid();

                            // Save FCM Token
                            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(taskToken -> {
                                if (taskToken.isSuccessful()) {
                                    String token = taskToken.getResult();
                                    DatabaseReference userTypeRef = FirebaseDatabase.getInstance(DB_URL)
                                            .getReference("User_Type")
                                            .child(uid);
                                    HashMap<String, Object> map = new HashMap<>();
                                    map.put("type", gender_txt);
                                    map.put("token", token);
                                    userTypeRef.updateChildren(map);
                                } else {
                                    Log.w("FCMToken", "Fetching FCM token failed", taskToken.getException());
                                }
                            });



                            // Save user info
                            DatabaseReference userInfoRef = FirebaseDatabase.getInstance("https://f-ecg-7df04-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("User_Info");
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("usernameString", email_txt);
                            userMap.put("passwordString", password_txt);
                            userMap.put("nameString", fullname);
                            userMap.put("mobileNumberString", mobile_txt);
                            userMap.put("emergencyMobileNumber2String", emergencyMobile_txt);
                            userMap.put("gender", gender_txt);
                            userMap.put("date", date);

                            userInfoRef.child(uid).setValue(userMap).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    // Unified counter entry
                                    DatabaseReference counterRef = FirebaseDatabase.getInstance("https://f-ecg-7df04-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("UserID_Counter");
                                    counterRef.get().addOnSuccessListener(snapshot -> {
                                        long nextCounter = snapshot.getChildrenCount() + 1;

                                        // Save user entry under UserID_Counter
                                        DatabaseReference userIdEntryRef = counterRef.child(String.valueOf(nextCounter));
                                        Map<String, Object> userIdEntry = new HashMap<>();
                                        userIdEntry.put("userid", uid);
                                        userIdEntry.put("usertype", gender_txt);
                                        userIdEntry.put("date", date);
                                        userIdEntry.put("name", fullname);
                                        userIdEntryRef.setValue(userIdEntry);

                                        // Save total counter in separate field
                                        DatabaseReference totalCounterRef = FirebaseDatabase.getInstance("https://f-ecg-7df04-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("TotalCounters");
                                        totalCounterRef.child("UserID_Counter").setValue(nextCounter);
                                    });

                                    userAPI userapi = userAPI.getInstance();
                                    userapi.setUserId(uid);
                                    userapi.setUserName(email_txt);
                                    userapi.setName(fullname);
                                    Toast.makeText(Registration.this, "Registration Successful", Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(Registration.this, login_page.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                                    finish();
                                } else {
                                    Toast.makeText(Registration.this, "Failed to save user data", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(Registration.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(Registration.this, "No internet connection", Toast.LENGTH_LONG).show();
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void login(View view) {
        Intent intent = new Intent(Registration.this, login_page.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
