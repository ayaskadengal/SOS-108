package com.example.emergency_department_tracking_system;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Start background SOS monitoring service
        Intent serviceIntent = new Intent(this, SOSMonitorService.class);
        startService(serviceIntent);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start the Main Activity after loading
                Intent intent = new Intent(MainActivity.this, login_page.class);
                startActivity(intent);
                finish();
            }
        }, 3000); // 3-second delay
    }
}
