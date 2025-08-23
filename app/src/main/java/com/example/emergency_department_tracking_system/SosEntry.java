package com.example.emergency_department_tracking_system;

public class SosEntry {
    private String patientId;
    private String status;

    public SosEntry() {}  // Needed for Firebase

    public SosEntry(String patientId, String status) {
        this.patientId = patientId;
        this.status = status;
    }

    public String getPatientId() {
        return patientId;
    }

    public String getStatus() {
        return status;
    }
}
