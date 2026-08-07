package com.seams.backend.application.dto;

public record ProfileRequest(
    String username,
    String displayName,
    String password
) {}
