package com.example.emergency_department_tracking_system;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SOSMonitorService extends Service {

    private static final String CHANNEL_ID = "sos_monitor_channel";
    private static final String NOTIF_CHANNEL_NAME = "SOS Monitor";
    private static final String TAG = "SOSMonitorService";

    private DatabaseReference sosRef;
    private String userType = "";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForegroundServiceNotification();

        fetchUserTypeAndStartListening();
    }

    private void fetchUserTypeAndStartListening() {
        String userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            Log.e(TAG, "User not logged in, stopping service.");
            stopSelf();
            return;
        }

        DatabaseReference userTypeRef = FirebaseDatabase.getInstance("https://f-ecg-7df04-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("User_Type")
                .child(userId);

        userTypeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    userType = snapshot.child("type").getValue(String.class);
                    Log.d(TAG, "Fetched user type: " + userType);

                    if ("Ambulance_Driver".equalsIgnoreCase(userType)) {
                        startListeningForSOS();
                    } else {
                        Log.d(TAG, "User is not Ambulance Driver. Stopping service.");
                        stopSelf();
                    }
                } else {
                    Log.d(TAG, "User_Type node does not exist. Stopping service.");
                    stopSelf();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to fetch user type: " + error.getMessage());
                stopSelf();
            }
        });
    }

    private void startListeningForSOS() {
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        sosRef = FirebaseDatabase.getInstance("https://f-ecg-7df04-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("sos")
                .child(today);

        sosRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot.exists()) {
                    String status = snapshot.child("status").getValue(String.class);
                    String patientId = snapshot.getKey();

                    if ("pending".equalsIgnoreCase(status)) {
                        Log.d(TAG, "Pending SOS detected for patientId: " + patientId);
                        sendNotification(patientId);
                    }
                }
            }

            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
            @Override public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "SOS database error: " + error.getMessage());
            }
        });

        Log.d(TAG, "Started listening to sos/" + today);
    }

    private void sendNotification(String patientId) {
        Intent intent = new Intent(this, AcceptDeclineActivity.class);
        intent.putExtra("patientId", patientId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("🚨 New SOS Alert!")
                .setContentText("Patient ID " + patientId + " needs immediate help.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }

    private void startForegroundServiceNotification() {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SOS Monitor Running")
                .setContentText("Listening for new emergencies...")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build();

        startForeground(1, notification);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    NOTIF_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand called");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed.");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
