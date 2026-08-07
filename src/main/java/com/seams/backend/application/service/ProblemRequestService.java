package com.seams.backend.application.service;

import com.seams.backend.core.model.ProblemRequest;
import com.seams.backend.core.model.Status;
import com.seams.backend.core.model.Student;
import com.seams.backend.core.model.User;
import com.seams.backend.core.repository.ProblemRequestRepository;
import com.seams.backend.core.repository.StudentRepository;
import com.seams.backend.core.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProblemRequestService {

    private final ProblemRequestRepository repository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    public ProblemRequestService(ProblemRequestRepository repository, StudentRepository studentRepository, UserRepository userRepository) {
        this.repository = repository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
    }

    public List<ProblemRequest> findAll() {
        return repository.findAll();
    }

    public List<ProblemRequest> findAllByStudent() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        Student student = studentRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        return repository.findAllByStudentIdOrderByCreatedAtDesc(student.getStudentId());
    }

    @CacheEvict(value = "stats", allEntries = true)
    public ProblemRequest submitRequest(String details) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        Student student = studentRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (repository.existsByStudentIdAndStatus(student.getStudentId(), Status.PENDING)) {
            throw new RuntimeException("Active Request Found: Please wait for your previous report to be processed.");
        }

        ProblemRequest request = ProblemRequest.builder()
                .studentId(student.getStudentId())
                .surname(student.getLastname())
                .firstname(student.getFirstname())
                .middlename(student.getMiddlename())
                .suffix(student.getSuffix())
                .program(student.getProgram())
                .year(student.getYear())
                .department(student.getDepartment())
                .details(details)
                .status(Status.PENDING)
                .createdAt(Instant.now())
                .trackingKey(UUID.randomUUID().toString())
                .build();

        return repository.save(request);
    }

    public Optional<ProblemRequest> findByTrackingKey(String key) {
        return repository.findByTrackingKey(key);
    }

    @CacheEvict(value = "stats", allEntries = true)
    public ProblemRequest updateStatus(Integer id, Status status, String adminReply) {
        ProblemRequest request = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        request.setStatus(status);
        request.setAdminReply(adminReply);
        return repository.save(request);
    }

    @Scheduled(fixedRate = 3600000) // Every hour
    public void cleanupOldRequests() {
        repository.deleteByCreatedAtBefore(Instant.now().minus(Duration.ofDays(2)));
    }
}
