package com.example.emergency_department_tracking_system;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.firebase.database.FirebaseDatabase;

public class NotificationActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String patientId = intent.getStringExtra("patientId");
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(new java.util.Date());

        if ("ACCEPT_SOS".equals(action)) {
            FirebaseDatabase.getInstance().getReference("sos").child(today).child(patientId).child("status").setValue("taken");
            Log.d("NotificationAction", "SOS accepted for patientId: " + patientId);

            // Open Ambulance Driver Home
            Intent i = new Intent(context, ambulance_driver_home_screen.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        } else if ("DECLINE_SOS".equals(action)) {
            Log.d("NotificationAction", "SOS declined for patientId: " + patientId);
        }
    }
}
