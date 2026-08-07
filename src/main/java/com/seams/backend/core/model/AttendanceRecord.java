package com.seams.backend.core.model;

import jakarta.persistence.*;

@Entity
public class AttendanceRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer eventId;
    private String studentId;
    private String timeIn;
    private String timeOut;
    private boolean isLate;
    private String status; // ATTENDED, ABSENT
    
    private String aseadoProfile; // Links to the profile/department that scanned it
    private java.time.Instant createdAt;

    public AttendanceRecord() {}
    public AttendanceRecord(Integer id, Integer eventId, String studentId, String timeIn, String timeOut, boolean isLate) {
        this.id = id;
        this.eventId = eventId;
        this.studentId = studentId;
        this.timeIn = timeIn;
        this.timeOut = timeOut;
        this.isLate = isLate;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getEventId() { return eventId; }
    public void setEventId(Integer eventId) { this.eventId = eventId; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getTimeIn() { return timeIn; }
    public void setTimeIn(String timeIn) { this.timeIn = timeIn; }
    public String getTimeOut() { return timeOut; }
    public void setTimeOut(String timeOut) { this.timeOut = timeOut; }
    public boolean isLate() { return isLate; }
    public void setLate(boolean late) { isLate = late; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getAseadoProfile() { return aseadoProfile; }
    public void setAseadoProfile(String aseadoProfile) { this.aseadoProfile = aseadoProfile; }
    public java.time.Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.Instant createdAt) { this.createdAt = createdAt; }
}
