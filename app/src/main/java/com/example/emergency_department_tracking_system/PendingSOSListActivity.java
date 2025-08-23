package com.example.emergency_department_tracking_system;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PendingSOSListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    TextView noDataText;
    SosAdapter adapter;
    List<SosEntry> pendingList = new ArrayList<>();
    DatabaseReference sosRef;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pending_soslist);

        recyclerView = findViewById(R.id.recyclerView);
        noDataText = findViewById(R.id.noDataText);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SosAdapter(pendingList, this);
        recyclerView.setAdapter(adapter);
        Button btnMyPendingWork = findViewById(R.id.btnMyPendingWork);
        btnMyPendingWork.setOnClickListener(v -> {
            DatabaseReference sosRefs = FirebaseDatabase.getInstance("https://f-ecg-7df04-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("ambulance_driver_status").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

sosRefs.addListenerForSingleValueEvent(new ValueEventListener() {
    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {
        if(snapshot.exists())
        {
             String status=snapshot.child("status").getValue(String.class);
             String patientId=snapshot.child("patientid").getValue(String.class);
             if(status.equals("taken")){
                 Intent intent=new Intent(PendingSOSListActivity.this,ambulance_driver_home_screen.class);
                 intent.putExtra("patientId", patientId);
                 startActivity(intent);

             }

        }
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }
});
            // Your action here
        });
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        sosRef = FirebaseDatabase.getInstance("https://f-ecg-7df04-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("sos").child(today);


        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        loadPendingSOS();
        swipeRefreshLayout.setOnRefreshListener(() -> {
            // Refresh your data
            loadPendingSOS();
            // Stop the refreshing indicator after data is loaded
            swipeRefreshLayout.setRefreshing(false);
        });
    }

    private void loadPendingSOS() {
        sosRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                pendingList.clear();

                for (DataSnapshot patientSnapshot : snapshot.getChildren()) {
                    String status = patientSnapshot.child("status").getValue(String.class);
                    if ("pending".equalsIgnoreCase(status)) {
                        pendingList.add(new SosEntry(patientSnapshot.getKey(), status));
                    }
                }

                if (pendingList.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    noDataText.setVisibility(View.VISIBLE);
                } else {
                    noDataText.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(PendingSOSListActivity.this, "Failed to load: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
