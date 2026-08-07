package com.seams.backend.core.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
    private String studentId;
    private String lastname;
    private String firstname;
    private String middlename;
    private String suffix;
    
    // Legacy fields for backward compatibility during migration
    private String year;
    private String program;
    private String department;
    
    // Transient fields for receiving full descriptive names from Node push requests
    @Transient
    private String departmentName;
    @Transient
    private String programName;

    private String recoveryCode;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL)
    private List<StudentEnrollment> enrollments;

    public Student() {}
    public Student(Integer id, User user, String studentId, String lastname, String firstname, String middlename, String suffix, String year, String program, String department, String recoveryCode) {
        this.id = id;
        this.user = user;
        this.studentId = studentId;
        this.lastname = lastname;
        this.firstname = firstname;
        this.middlename = middlename;
        this.suffix = suffix;
        this.year = year;
        this.program = program;
        this.department = department;
        this.recoveryCode = recoveryCode;
    }

    public static StudentBuilder builder() { return new StudentBuilder(); }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getLastname() { return lastname; }
    public void setLastname(String lastname) { this.lastname = lastname; }
    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }
    public String getMiddlename() { return middlename; }
    public void setMiddlename(String middlename) { this.middlename = middlename; }
    public String getSuffix() { return suffix; }
    public void setSuffix(String suffix) { this.suffix = suffix; }
    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
    public String getProgram() { return program; }
    public void setProgram(String program) { this.program = program; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getRecoveryCode() { return recoveryCode; }
    public void setRecoveryCode(String recoveryCode) { this.recoveryCode = recoveryCode; }
    public List<StudentEnrollment> getEnrollments() { return enrollments; }
    public void setEnrollments(List<StudentEnrollment> enrollments) { this.enrollments = enrollments; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }
    public String getProgramName() { return programName; }
    public void setProgramName(String programName) { this.programName = programName; }

    public static class StudentBuilder {
        private Integer id;
        private User user;
        private String studentId;
        private String lastname;
        private String firstname;
        private String middlename;
        private String suffix;
        private String year;
        private String program;
        private String department;
        private String departmentName;
        private String programName;
        private String recoveryCode;

        public StudentBuilder id(Integer id) { this.id = id; return this; }
        public StudentBuilder user(User user) { this.user = user; return this; }
        public StudentBuilder studentId(String studentId) { this.studentId = studentId; return this; }
        public StudentBuilder lastname(String lastname) { this.lastname = lastname; return this; }
        public StudentBuilder firstname(String firstname) { this.firstname = firstname; return this; }
        public StudentBuilder middlename(String middlename) { this.middlename = middlename; return this; }
        public StudentBuilder suffix(String suffix) { this.suffix = suffix; return this; }
        public StudentBuilder year(String year) { this.year = year; return this; }
        public StudentBuilder program(String program) { this.program = program; return this; }
        public StudentBuilder department(String department) { this.department = department; return this; }
        public StudentBuilder departmentName(String departmentName) { this.departmentName = departmentName; return this; }
        public StudentBuilder programName(String programName) { this.programName = programName; return this; }
        public StudentBuilder recoveryCode(String recoveryCode) { this.recoveryCode = recoveryCode; return this; }
        public Student build() {
            Student s = new Student(id, user, studentId, lastname, firstname, middlename, suffix, year, program, department, recoveryCode);
            s.setDepartmentName(departmentName);
            s.setProgramName(programName);
            return s;
        }
    }
}
