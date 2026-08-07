package com.seams.backend.application.dto;

public record AuthenticationResponse(
    String token,
    String role,
    String displayName,
    boolean mustChangePassword
) {
    public static AuthenticationResponseBuilder builder() {
        return new AuthenticationResponseBuilder();
    }

    public static class AuthenticationResponseBuilder {
        private String token;
        private String role;
        private String displayName;
        private boolean mustChangePassword;

        public AuthenticationResponseBuilder token(String token) { this.token = token; return this; }
        public AuthenticationResponseBuilder role(String role) { this.role = role; return this; }
        public AuthenticationResponseBuilder displayName(String displayName) { this.displayName = displayName; return this; }
        public AuthenticationResponseBuilder mustChangePassword(boolean mustChangePassword) { this.mustChangePassword = mustChangePassword; return this; }
        public AuthenticationResponse build() {
            return new AuthenticationResponse(token, role, displayName, mustChangePassword);
        }
    }
}
