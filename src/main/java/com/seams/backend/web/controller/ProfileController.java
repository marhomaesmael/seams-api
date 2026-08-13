package com.seams.backend.web.controller;

import com.seams.backend.core.model.*;
import com.seams.backend.core.repository.*;
import com.seams.backend.application.service.UserDetails;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/profile")
public class ProfileController {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final StudentEnrollmentRepository enrollmentRepository;
    private final AttendanceRecordRepository attendanceRepository;
    private final EventRepository eventRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileController(UserRepository userRepository, StudentRepository studentRepository, 
                             StudentEnrollmentRepository enrollmentRepository,
                             AttendanceRecordRepository attendanceRepository,
                             EventRepository eventRepository,
                             PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.attendanceRepository = attendanceRepository;
        this.eventRepository = eventRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public Map<String, Object> getProfile() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        
        Map<String, Object> response = new HashMap<>();
        response.put("username", user.getUsername());
        response.put("displayName", user.getDisplayName());
        response.put("role", user.getRole());
        response.put("googleEmail", user.getGoogleEmail());

        if (user.getRole() == Role.STUDENT) {
            studentRepository.findByUser(user).ifPresent(student -> {
                Map<String, Object> studentMap = new HashMap<>();
                studentMap.put("studentId", student.getStudentId());
                studentMap.put("firstname", student.getFirstname());
                studentMap.put("lastname", student.getLastname());
                studentMap.put("department", student.getDepartment());
                studentMap.put("program", student.getProgram());
                studentMap.put("year", student.getYear());
                studentMap.put("recoveryCode", student.getRecoveryCode());
                response.put("student", studentMap);
                
                List<StudentEnrollment> enrollments = enrollmentRepository.findByStudent(student);
                response.put("enrollments", enrollments.stream().map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("academicYear", e.getAcademicYear());
                    m.put("semester", e.getSemester());
                    m.put("department", e.getDepartment().getName());
                    m.put("deptCode", e.getDepartment().getCode());
                    m.put("program", e.getProgram().getName());
                    m.put("yearLevel", e.getYearLevel().getLevel());
                    return m;
                }).collect(Collectors.toList()));
                
                List<AttendanceRecord> records = attendanceRepository.findByStudentId(student.getStudentId());
                Map<String, List<Map<String, Object>>> history = records.stream()
                        .collect(Collectors.groupingBy(
                                AttendanceRecord::getAseadoProfile,
                                Collectors.mapping(r -> {
                                    Event event = eventRepository.findById(r.getEventId()).orElse(null);
                                    Map<String, Object> m = new HashMap<>();
                                    m.put("eventName", event != null ? event.getName() : "Unknown Event");
                                    m.put("eventDate", event != null ? event.getEventDate() : "-");
                                    m.put("timeIn", r.getTimeIn());
                                    m.put("timeOut", r.getTimeOut());
                                    m.put("isLate", r.isLate());
                                    m.put("status", r.getStatus());
                                    m.put("hasLogout", event != null ? event.isHasLogout() : true);
                                    m.put("createdAt", r.getCreatedAt());
                                    return m;
                                }, Collectors.toList())
                        ));
                response.put("attendanceHistory", history);
            });
        }

        return response;
    }

    @PutMapping
    public void updateProfile(@RequestBody Map<String, String> request) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findByUsername(userDetails.getUsername()).orElseThrow();
        
        if (request.containsKey("displayName")) {
            user.setDisplayName(request.get("displayName"));
        }
        
        if (request.containsKey("username")) {
            user.setUsername(request.get("username"));
        }
        
        if (request.containsKey("password") && request.get("password") != null && !request.get("password").isBlank()) {
            user.setPassword(passwordEncoder.encode(request.get("password")));
        }
        
        userRepository.save(user);
    }
}
