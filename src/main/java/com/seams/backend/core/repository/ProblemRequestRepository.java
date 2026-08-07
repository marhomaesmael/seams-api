package com.seams.backend.core.repository;

import com.seams.backend.core.model.ProblemRequest;
import com.seams.backend.core.model.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ProblemRequestRepository extends JpaRepository<ProblemRequest, Integer> {
    Optional<ProblemRequest> findTopByStudentIdOrderByCreatedAtDesc(String studentId);
    Optional<ProblemRequest> findByTrackingKey(String trackingKey);
    List<ProblemRequest> findAllByStudentIdOrderByCreatedAtDesc(String studentId);
    boolean existsByStudentIdAndStatus(String studentId, Status status);

    @Transactional
    void deleteByCreatedAtBefore(Instant timestamp);
}
