package com.seams.backend.web.controller;

import com.seams.backend.application.dto.AuthenticationRequest;
import com.seams.backend.application.dto.AuthenticationResponse;
import com.seams.backend.application.service.AuthService;
import com.seams.backend.application.service.ProblemRequestService;
import com.seams.backend.core.model.ProblemRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService service;
    private final ProblemRequestService requestService;

    public AuthController(AuthService service, ProblemRequestService requestService) {
        this.service = service;
        this.requestService = requestService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(service.authenticate(request));
    }

    @PostMapping("/problem-request")
    public ResponseEntity<ProblemRequest> submitProblem(@RequestBody Map<String, String> body) {
        return ResponseEntity.ok(service.submitProblemRequest(body));
    }

    @GetMapping("/problem-request/track/{key}")
    public ResponseEntity<ProblemRequest> trackRequest(@PathVariable String key) {
        return requestService.findByTrackingKey(key)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> body) {
        service.resetPassword(body.get("studentId"), body.get("recoveryCode"), body.get("newPassword"));
        return ResponseEntity.ok("Password reset successful");
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(@RequestBody Map<String, String> body) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String recoveryCode = service.changePassword(userDetails.getUsername(), body.get("newPassword"));
        return ResponseEntity.ok(Map.of(
            "message", "Password updated successfully",
            "recoveryCode", recoveryCode != null ? recoveryCode : ""
        ));
    }
}
