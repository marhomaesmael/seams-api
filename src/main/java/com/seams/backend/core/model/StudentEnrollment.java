package com.seams.backend.core.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
public class StudentEnrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "student_id")
    @JsonIgnore
    private Student student;

    private String academicYear; // e.g., "2026-2027"
    private String semester; // e.g., "1st Semester"

    @ManyToOne
    @JoinColumn(name = "program_id")
    private Program program;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne
    @JoinColumn(name = "year_level_id")
    private YearLevel yearLevel;

    public StudentEnrollment() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public Program getProgram() { return program; }
    public void setProgram(Program program) { this.program = program; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public YearLevel getYearLevel() { return yearLevel; }
    public void setYearLevel(YearLevel yearLevel) { this.yearLevel = yearLevel; }
}
