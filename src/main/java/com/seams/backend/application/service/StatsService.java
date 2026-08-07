package com.seams.backend.application.service;

import com.seams.backend.core.model.Status;
import com.seams.backend.core.model.User;
import com.seams.backend.core.repository.*;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class StatsService {

    private final StudentRepository studentRepository;
    private final EventRepository eventRepository;
    private final ProblemRequestRepository problemRequestRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final UserRepository userRepository;

    public StatsService(StudentRepository studentRepository, 
                        EventRepository eventRepository, 
                        ProblemRequestRepository problemRequestRepository,
                        AttendanceRecordRepository attendanceRecordRepository,
                        UserRepository userRepository) {
        this.studentRepository = studentRepository;
        this.eventRepository = eventRepository;
        this.problemRequestRepository = problemRequestRepository;
        this.attendanceRecordRepository = attendanceRecordRepository;
        this.userRepository = userRepository;
    }

    @Cacheable(value = "stats", key = "'admin'")
    public Map<String, Object> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("studentCount", studentRepository.count());
        stats.put("eventCount", eventRepository.count());
        stats.put("pendingApprovals", eventRepository.findAll().stream().filter(e -> "PENDING".equals(e.getStatus())).count());
        stats.put("activeIssues", problemRequestRepository.findAll().stream().filter(r -> r.getStatus() == Status.PENDING).count());
        return stats;
    }

    @Cacheable(value = "stats", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    public Map<String, Object> getStudentStats() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();

        Map<String, Object> stats = new HashMap<>();
        studentRepository.findByUser(user).ifPresent(student -> {
            // Count total number of accepted events where the student has an attendance record (either ATTENDED or ABSENT)
            var allAcceptedRecords = attendanceRecordRepository.findByStudentId(student.getStudentId()).stream()
                    .filter(r -> eventRepository.findById(r.getEventId())
                            .map(e -> "ACCEPTED".equals(e.getStatus()))
                            .orElse(false))
                    .toList();

            long attendedCount = allAcceptedRecords.stream()
                    .filter(r -> "ATTENDED".equals(r.getStatus()))
                    .count();
            
            stats.put("attendedEvents", (int) attendedCount);
            
            long totalRelevantEvents = allAcceptedRecords.size();
            
            double rate = totalRelevantEvents == 0 ? 0 : (double) attendedCount / totalRelevantEvents * 100;
            stats.put("attendanceRate", Math.round(rate));
            
            String standing = rate >= 80 ? "EXCELLENT" : (rate >= 60 ? "GOOD" : "AT RISK");
            stats.put("standing", standing);
        });
        
        return stats;
    }
}
