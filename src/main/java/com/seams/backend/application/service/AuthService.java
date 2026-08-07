package com.seams.backend.application.service;

import com.seams.backend.infrastructure.security.JwtService;
import com.seams.backend.application.dto.AuthenticationRequest;
import com.seams.backend.application.dto.AuthenticationResponse;
import com.seams.backend.core.model.*;
import com.seams.backend.core.repository.ProblemRequestRepository;
import com.seams.backend.core.repository.StudentRepository;
import com.seams.backend.core.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final ProblemRequestRepository problemRequestRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, StudentRepository studentRepository, 
                       ProblemRequestRepository problemRequestRepository,
                       PasswordEncoder passwordEncoder, JwtService jwtService, 
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.studentRepository = studentRepository;
        this.problemRequestRepository = problemRequestRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.username(),
                        request.password()
                )
        );
        var user = userRepository.findByUsername(request.username())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .role(user.getRole().name())
                .displayName(user.getDisplayName())
                .mustChangePassword(user.isMustChangePassword())
                .build();
    }

    public ProblemRequest submitProblemRequest(Map<String, String> body) {
        String studentId = body.get("studentId");
        
        if (problemRequestRepository.existsByStudentIdAndStatus(studentId, Status.PENDING)) {
            throw new RuntimeException("Security Protocol: Active Request Found. Please wait for an administrator to review your existing report.");
        }

        ProblemRequest request = ProblemRequest.builder()
                .studentId(studentId)
                .surname(body.get("surname"))
                .firstname(body.get("firstname"))
                .middlename(body.get("middlename"))
                .suffix(body.get("suffix"))
                .program(body.get("program"))
                .year(body.get("year"))
                .department(body.get("department"))
                .details(body.get("details"))
                .status(Status.PENDING)
                .createdAt(Instant.now())
                .trackingKey(UUID.randomUUID().toString())
                .build();
        
        return problemRequestRepository.save(request);
    }

    @Transactional
    @CacheEvict(value = "students", allEntries = true)
    public void resetPassword(String studentId, String recoveryCode, String newPassword) {
        Student student = studentRepository.findByStudentIdAndRecoveryCode(studentId, recoveryCode)
                .orElseThrow(() -> new RuntimeException("Identity verification failed: Invalid ID or Recovery Code."));
        
        User user = student.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        userRepository.save(user);
    }

    @Transactional
    @CacheEvict(value = "students", allEntries = true)
    public String changePassword(String username, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        userRepository.save(user);

        return studentRepository.findByUser(user)
                .map(Student::getRecoveryCode)
                .orElse(null);
    }
}
