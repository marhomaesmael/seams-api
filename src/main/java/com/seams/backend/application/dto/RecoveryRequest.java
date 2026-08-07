package com.seams.backend.application.dto;

public record RecoveryRequest(
    String studentId,
    String recoveryCode,
    String newPassword
) {}
