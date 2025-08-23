package com.example.emergency_department_tracking_system;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SosAdapter extends RecyclerView.Adapter<SosAdapter.ViewHolder> {
    private List<SosEntry> sosList;
    private Context context;

    public SosAdapter(List<SosEntry> sosList, Context context) {
        this.sosList = sosList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_sos, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SosEntry entry = sosList.get(position);
        holder.patientIdText.setText("Patient ID: " + entry.getPatientId());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, AcceptDeclineActivity.class);
            intent.putExtra("patientId", entry.getPatientId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return sosList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView patientIdText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            patientIdText = itemView.findViewById(R.id.patientIdText);
        }
    }
}
