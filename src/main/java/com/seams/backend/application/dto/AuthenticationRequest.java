package com.seams.backend.application.dto;

public record AuthenticationRequest(
    String username,
    String password
) {}
