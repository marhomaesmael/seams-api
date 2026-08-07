package com.seams.backend.core.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class ProblemRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String studentId;
    private String surname;
    private String firstname;
    private String middlename;
    private String suffix;
    private String program;
    private String year;
    private String department;
    private String details;
    @Enumerated(EnumType.STRING)
    private Status status;
    private String adminReply;
    private String trackingKey;
    private Instant cooldownExpiry;
    private Instant createdAt;

    public ProblemRequest() {}
    public ProblemRequest(Integer id, String studentId, String surname, String firstname, String middlename, String suffix, String program, String year, String department, String details, Status status, String adminReply, String trackingKey, Instant cooldownExpiry, Instant createdAt) {
        this.id = id;
        this.studentId = studentId;
        this.surname = surname;
        this.firstname = firstname;
        this.middlename = middlename;
        this.suffix = suffix;
        this.program = program;
        this.year = year;
        this.department = department;
        this.details = details;
        this.status = status;
        this.adminReply = adminReply;
        this.trackingKey = trackingKey;
        this.cooldownExpiry = cooldownExpiry;
        this.createdAt = createdAt;
    }

    public static ProblemRequestBuilder builder() { return new ProblemRequestBuilder(); }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    
    public String getStudentName() {
        return (firstname != null ? firstname : "") + " " + (surname != null ? surname : "");
    }

    public String getSurname() { return surname; }
    public void setSurname(String surname) { this.surname = surname; }
    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }
    public String getMiddlename() { return middlename; }
    public void setMiddlename(String middlename) { this.middlename = middlename; }
    public String getSuffix() { return suffix; }
    public void setSuffix(String suffix) { this.suffix = suffix; }
    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }
    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public String getAdminReply() { return adminReply; }
    public void setAdminReply(String adminReply) { this.adminReply = adminReply; }
    public String getTrackingKey() { return trackingKey; }
    public void setTrackingKey(String trackingKey) { this.trackingKey = trackingKey; }
    public Instant getCooldownExpiry() { return cooldownExpiry; }
    public void setCooldownExpiry(Instant cooldownExpiry) { this.cooldownExpiry = cooldownExpiry; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public static class ProblemRequestBuilder {
        private Integer id;
        private String studentId;
        private String surname;
        private String firstname;
        private String middlename;
        private String suffix;
        private String program;
        private String year;
        private String department;
        private String details;
        private Status status;
        private String adminReply;
        private String trackingKey;
        private Instant cooldownExpiry;
        private Instant createdAt;

        public ProblemRequestBuilder id(Integer id) { this.id = id; return this; }
        public ProblemRequestBuilder studentId(String studentId) { this.studentId = studentId; return this; }
        public ProblemRequestBuilder surname(String surname) { this.surname = surname; return this; }
        public ProblemRequestBuilder firstname(String firstname) { this.firstname = firstname; return this; }
        public ProblemRequestBuilder middlename(String middlename) { this.middlename = middlename; return this; }
        public ProblemRequestBuilder suffix(String suffix) { this.suffix = suffix; return this; }
        public ProblemRequestBuilder program(String program) { this.program = program; return this; }
        public ProblemRequestBuilder year(String year) { this.year = year; return this; }
        public ProblemRequestBuilder department(String department) { this.department = department; return this; }
        public ProblemRequestBuilder details(String details) { this.details = details; return this; }
        public ProblemRequestBuilder status(Status status) { this.status = status; return this; }
        public ProblemRequestBuilder adminReply(String adminReply) { this.adminReply = adminReply; return this; }
        public ProblemRequestBuilder trackingKey(String trackingKey) { this.trackingKey = trackingKey; return this; }
        public ProblemRequestBuilder cooldownExpiry(Instant cooldownExpiry) { this.cooldownExpiry = cooldownExpiry; return this; }
        public ProblemRequestBuilder createdAt(Instant createdAt) { this.createdAt = createdAt; return this; }
        public ProblemRequest build() {
            return new ProblemRequest(id, studentId, surname, firstname, middlename, suffix, program, year, department, details, status, adminReply, trackingKey, cooldownExpiry, createdAt);
        }
    }
}
