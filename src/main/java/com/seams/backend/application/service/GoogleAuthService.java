package com.seams.backend.application.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.seams.backend.application.dto.AuthenticationResponse;
import com.seams.backend.core.model.User;
import com.seams.backend.core.repository.UserRepository;
import com.seams.backend.infrastructure.security.JwtService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Service
public class GoogleAuthService {

    @Value("${application.security.google.client-id}")
    private String clientId;

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public GoogleAuthService(UserRepository userRepository, JwtService jwtService, 
                             PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public enum GoogleAuthStatus {
        SUCCESS,
        NEEDS_LINKING,
        LINK_CONFLICT,
        ERROR
    }

    public record GoogleAuthResult(GoogleAuthStatus status, AuthenticationResponse authResponse, String googleEmail, String existingLinkedEmail) {}

    public GoogleAuthResult loginWithGoogle(String idTokenString) {
        try {
            GoogleIdToken.Payload payload = verifyToken(idTokenString);
            if (payload == null) return new GoogleAuthResult(GoogleAuthStatus.ERROR, null, null, null);

            String email = payload.getEmail();
            Optional<User> userOpt = userRepository.findByGoogleEmail(email);

            if (userOpt.isPresent()) {
                User user = userOpt.get();
                var jwtToken = jwtService.generateToken(user);
                AuthenticationResponse authResponse = AuthenticationResponse.builder()
                        .token(jwtToken)
                        .role(user.getRole().name())
                        .displayName(user.getDisplayName())
                        .mustChangePassword(user.isMustChangePassword())
                        .build();
                return new GoogleAuthResult(GoogleAuthStatus.SUCCESS, authResponse, email, null);
            } else {
                return new GoogleAuthResult(GoogleAuthStatus.NEEDS_LINKING, null, email, null);
            }
        } catch (Exception e) {
            return new GoogleAuthResult(GoogleAuthStatus.ERROR, null, null, null);
        }
    }

    @Transactional
    public GoogleAuthResult linkAccount(String idTokenString, String username, String password, boolean force) {
        try {
            GoogleIdToken.Payload payload = verifyToken(idTokenString);
            if (payload == null) return new GoogleAuthResult(GoogleAuthStatus.ERROR, null, null, null);

            String email = payload.getEmail();
            
            // Check if this Google account is already linked to ANOTHER user
            Optional<User> existingGoogleUser = userRepository.findByGoogleEmail(email);
            if (existingGoogleUser.isPresent()) {
                 return new GoogleAuthResult(GoogleAuthStatus.ERROR, null, null, null); // Email already used
            }

            // Authenticate the SEAMS account
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
            User user = userRepository.findByUsername(username).orElseThrow();

            // Conflict Check: Is this SEAMS account already linked to a DIFFERENT Google account?
            if (user.getGoogleEmail() != null && !user.getGoogleEmail().equals(email) && !force) {
                return new GoogleAuthResult(GoogleAuthStatus.LINK_CONFLICT, null, email, user.getGoogleEmail());
            }

            // Link the account
            user.setGoogleEmail(email);
            userRepository.save(user);

            var jwtToken = jwtService.generateToken(user);
            AuthenticationResponse authResponse = AuthenticationResponse.builder()
                    .token(jwtToken)
                    .role(user.getRole().name())
                    .displayName(user.getDisplayName())
                    .mustChangePassword(user.isMustChangePassword())
                    .build();
            
            return new GoogleAuthResult(GoogleAuthStatus.SUCCESS, authResponse, email, null);
        } catch (Exception e) {
            return new GoogleAuthResult(GoogleAuthStatus.ERROR, null, null, null);
        }
    }

    @Transactional
    public void connectGoogle(String idTokenString, String username) {
        try {
            GoogleIdToken.Payload payload = verifyToken(idTokenString);
            if (payload == null) throw new RuntimeException("Invalid Google Token");

            String email = payload.getEmail();
            
            // Check if this Google email is already linked elsewhere
            userRepository.findByGoogleEmail(email).ifPresent(u -> {
                if (!u.getUsername().equals(username)) {
                    throw new RuntimeException("This Google account is already connected to another SEAMS identity.");
                }
            });

            User user = userRepository.findByUsername(username).orElseThrow();
            user.setGoogleEmail(email);
            userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Transactional
    public void disconnectGoogle(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        user.setGoogleEmail(null);
        userRepository.save(user);
    }

    private GoogleIdToken.Payload verifyToken(String idTokenString) throws Exception {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(clientId))
                .build();

        GoogleIdToken idToken = verifier.verify(idTokenString);
        return (idToken != null) ? idToken.getPayload() : null;
    }
}
