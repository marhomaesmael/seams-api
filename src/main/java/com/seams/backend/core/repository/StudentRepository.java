package com.seams.backend.core.repository;

import com.seams.backend.core.model.Student;
import com.seams.backend.core.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Integer> {
    Optional<Student> findByStudentId(String studentId);
    Optional<Student> findByUser(User user);
    Optional<Student> findByStudentIdAndRecoveryCode(String studentId, String recoveryCode);

    @EntityGraph(attributePaths = {"enrollments"})
    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT s FROM Student s " +
            "WHERE LOWER(s.studentId) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(s.firstname) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(s.lastname) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<Student> search(@org.springframework.data.repository.query.Param("query") String query, Pageable pageable);

    @EntityGraph(attributePaths = {"enrollments"})
    @org.springframework.data.jpa.repository.Query("SELECT DISTINCT s FROM Student s " +
            "LEFT JOIN s.enrollments e " +
            "WHERE LOWER(s.department) LIKE LOWER(CONCAT('%', :dept, '%')) " +
            "OR LOWER(e.department.code) LIKE LOWER(CONCAT('%', :dept, '%')) " +
            "OR LOWER(e.department.name) LIKE LOWER(CONCAT('%', :dept, '%')) " +
            "OR LOWER(:dept) LIKE LOWER(CONCAT('%', s.department, '%')) " +
            "OR LOWER(:dept) LIKE LOWER(CONCAT('%', e.department.code, '%'))")
    Page<Student> findAllByDepartment(@org.springframework.data.repository.query.Param("dept") String dept, Pageable pageable);

    @EntityGraph(attributePaths = {"enrollments"})
    @org.springframework.data.jpa.repository.Query(value = "SELECT DISTINCT s FROM Student s",
            countQuery = "SELECT count(s) FROM Student s")
    Page<Student> findAllWithEnrollments(Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT s.studentId FROM Student s " +
            "WHERE LOWER(s.studentId) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(s.firstname) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(s.lastname) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<String> searchIds(@org.springframework.data.repository.query.Param("query") String query);

    @org.springframework.data.jpa.repository.Query("SELECT new com.seams.backend.application.dto.StudentSyncDto(s.id, s.studentId, s.firstname, s.lastname, s.middlename, s.suffix, s.department, s.year, s.program) " +
            "FROM Student s WHERE LOWER(s.department) LIKE LOWER(CONCAT('%', :dept, '%'))")
    List<com.seams.backend.application.dto.StudentSyncDto> findSyncListByDepartment(@org.springframework.data.repository.query.Param("dept") String dept);

    List<Student> findAllByStudentIdIn(java.util.Collection<String> studentIds);
}
