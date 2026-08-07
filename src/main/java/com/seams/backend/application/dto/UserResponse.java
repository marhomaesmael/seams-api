package com.seams.backend.application.dto;

public record UserResponse(
    Integer id,
    String username,
    String displayName,
    String role
) {}
