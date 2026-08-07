package com.seams.backend.core.repository;

import com.seams.backend.core.model.Department;
import com.seams.backend.core.model.Student;
import com.seams.backend.core.model.StudentEnrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface StudentEnrollmentRepository extends JpaRepository<StudentEnrollment, Integer> {
    List<StudentEnrollment> findByStudent(Student student);
    List<StudentEnrollment> findByStudent_StudentId(String studentId);
    
    @Transactional
    @Modifying
    @Query("DELETE FROM StudentEnrollment se WHERE se.department = :department")
    void deleteByDepartment(Department department);

    @Transactional
    @Modifying
    void deleteByStudent(Student student);
}
