package com.seams.backend.web.controller;

import com.seams.backend.application.service.GoogleAuthService;
import com.seams.backend.application.service.GoogleAuthService.GoogleAuthResult;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/google")
public class GoogleAuthController {

    private final GoogleAuthService googleAuthService;

    public GoogleAuthController(GoogleAuthService googleAuthService) {
        this.googleAuthService = googleAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<GoogleAuthResult> login(@RequestBody Map<String, String> request) {
        return ResponseEntity.ok(googleAuthService.loginWithGoogle(request.get("idToken")));
    }

    @PostMapping("/link")
    public ResponseEntity<GoogleAuthResult> link(@RequestBody Map<String, Object> request) {
        String idToken = (String) request.get("idToken");
        String username = (String) request.get("username");
        String password = (String) request.get("password");
        boolean force = request.get("force") != null && (boolean) request.get("force");
        
        return ResponseEntity.ok(googleAuthService.linkAccount(idToken, username, password, force));
    }

    @PostMapping("/connect")
    public ResponseEntity<Void> connect(@RequestBody Map<String, String> request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        googleAuthService.connectGoogle(request.get("idToken"), username);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/disconnect")
    public ResponseEntity<Void> disconnect() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        googleAuthService.disconnectGoogle(username);
        return ResponseEntity.ok().build();
    }
}
