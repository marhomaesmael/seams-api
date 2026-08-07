package com.seams.backend.application.dto;

public record StudentSyncDto(
    String studentId,
    String firstname,
    String lastname,
    String department
) {}
