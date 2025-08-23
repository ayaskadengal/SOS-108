package com.example.emergency_department_tracking_system;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import util.userAPI;

public class login_page extends AppCompatActivity {
    private EditText userName;
    private EditText password;
    private Button login;
    private TextView forgotPword;
    private FirebaseAuth auth;
    private ConstraintLayout parent;
    private TextView notVerified;
    public String nameFromDB;
    //    public  String permissionFromDB;
    userAPI userapi = userAPI.getInstance();
//    public String permission="disagree";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        getSupportActionBar().hide();


        auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        //  if((user!=null )&& (user.isEmailVerified())){
        // startActivity(new Intent(Login_page.this,home_screen.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
//                    finish();
        // }

        View decorView = getWindow().getDecorView();
// Hide both the navigation bar and the status bar.
// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
// a general rule, you should design your app to hide the status bar whenever you
// hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        userName = findViewById(R.id.userName);
        password = findViewById(R.id.password);
        login = findViewById(R.id.login_bt);
        forgotPword = findViewById(R.id.forgot_password);
        parent = findViewById(R.id.parentLayout);
        notVerified = findViewById(R.id.notVerified);
        auth = FirebaseAuth.getInstance();

        FirebaseUser userss = auth.getCurrentUser();
        if ((userss != null) && (userss.isEmailVerified())) {

            // if (!isMyServiceRunning(mYourService.getClass())) {
            //   startService(mServiceIntent);
            //  }
            DatabaseReference sdd=FirebaseDatabase.getInstance("https://f-ecg-7df04-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("User_Type").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
sdd.addListenerForSingleValueEvent(new ValueEventListener() {
    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        String user_type=snapshot.child("type").getValue(String.class);
        Log.d("type","type" +user_type);
        Log.d("CHECK_USER_TYPE", "user_type = " + user_type + ", equals Ambulance_Driver? " + user_type.equals("Ambulance_Driver"));

        if(user_type.equals("Ambulance_Driver"))
        {
            Intent serviceIntent = new Intent(login_page.this, SOSMonitorService.class);
            startService(serviceIntent);
            startActivity(new Intent(login_page.this, PendingSOSListActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

        }
        else
        {
            startActivity(new Intent(login_page.this,EMTForm.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }
});

        }


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userName_txt = userName.getText().toString();
                String pword = password.getText().toString();
//                Toast.makeText(login_activity.this,userName_txt,Toast.LENGTH_LONG).show();
//               Toast.makeText(login_activity.this,pword,Toast.LENGTH_LONG).show();


                if (userName_txt.isEmpty()) {
                    userName.setError(getString(R.string.input_error_uname));
                    userName.requestFocus();
                    return;
                } else if (pword.isEmpty()) {
                    password.setError(getString(R.string.input_error_password));
                    password.requestFocus();
                    return;
                } else {
                    auth.signInWithEmailAndPassword(userName_txt, pword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                FirebaseUser user = auth.getCurrentUser();
                                String currUser = user.getUid();
                                DatabaseReference reference = FirebaseDatabase.getInstance("https://f-ecg-7df04-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("User");
                                Query checkUser = reference.orderByChild("email").equalTo(userName_txt);
                                checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            nameFromDB = snapshot.child(currUser).child("fullName").getValue(String.class);

                                            userapi.setName(nameFromDB);

                                            Log.d("user", "s" + user);


                                        }

                                    }

                                    @Override
                                    public void onCancelled(DatabaseError error) {

                                    }
                                });


                                userapi.setUserId(user.getUid());
                                userapi.setUserName(userName_txt);


                                assert user != null;
                                if (!user.isEmailVerified()) {
                                    Log.d("n", "signed");
                                    notVerified.setVisibility(View.VISIBLE);
                                    user.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(login_page.this, "Verification Email has been sent", Toast.LENGTH_LONG).show();


                                        }
                                    });


                                } else {


                                    Log.d("not", "signed");
                                    DatabaseReference sdd=FirebaseDatabase.getInstance("https://f-ecg-7df04-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("User_Type").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                    sdd.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            String user_type=snapshot.child("type").getValue(String.class);
                                            Log.d("type","type" +user_type);
                                            if(user_type.equals("Ambulance_Driver"))
                                            {
                                                Intent serviceIntent = new Intent(login_page.this, SOSMonitorService.class);
                                                startService(serviceIntent);
                                                startActivity(new Intent(login_page.this, PendingSOSListActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

                                            }
                                            else
                                            {
                                                startActivity(new Intent(login_page.this,EMTForm.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });

                                }


                            } else {
                                Toast.makeText(login_page.this, "Login failed" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

            }


            private void loginUser(String userName_txt, String pword) {
                auth.signInWithEmailAndPassword(userName_txt, pword).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        Toast.makeText(login_page.this, "Login successfull", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(login_page.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                });
            }
        });


    }


    public void create_account(View view) {
        Intent intent = new Intent(this, Registration.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    //  public void reset_password(View view) {
    //       Intent intent = new Intent(this, forget_password.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    //       startActivity(intent);
//    }

    @Override
    protected void onStart() {
        super.onStart();

    }
    public void home(View view) {
        Intent intent = new Intent(this, login_page.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}


