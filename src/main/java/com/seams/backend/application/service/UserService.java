package com.seams.backend.application.service;

import com.seams.backend.core.model.Role;
import com.seams.backend.core.model.Student;
import com.seams.backend.core.model.User;
import com.seams.backend.core.repository.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repository;
    private final StudentRepository studentRepository;
    private final StudentEnrollmentRepository enrollmentRepository;
    private final AttendanceRecordRepository attendanceRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository repository, StudentRepository studentRepository, 
                       StudentEnrollmentRepository enrollmentRepository,
                       AttendanceRecordRepository attendanceRepository,
                       PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.attendanceRepository = attendanceRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<User> findAll(String search, Pageable pageable) {
        if (search != null && !search.isBlank()) {
            return repository.search(search, pageable);
        }
        return repository.findAll(pageable);
    }

    @Transactional
    public void delete(Integer id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.getRole() == Role.SUPERVISOR) {
            throw new RuntimeException("Security Violation: Deletion of Supervisor accounts is strictly prohibited.");
        }

        // If it's a student, we must handle the Student record to avoid FK constraint issues
        studentRepository.findByUser(user).ifPresent(student -> {
            enrollmentRepository.deleteByStudent(student);
            attendanceRepository.deleteByStudentId(student.getStudentId());
            studentRepository.delete(student);
        });

        repository.delete(user);
    }

    @Transactional
    public void forcePasswordReset(Integer id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        String resetPassword;
        Optional<Student> studentOpt = studentRepository.findByUser(user);
        if (studentOpt.isPresent()) {
            resetPassword = studentOpt.get().getLastname().toUpperCase();
        } else {
            resetPassword = user.getUsername().toUpperCase();
        }

        user.setPassword(passwordEncoder.encode(resetPassword));
        user.setMustChangePassword(true);
        repository.save(user);
    }
}
