package com.seams.backend.application.dto;

public record StudentSyncDto(
    Integer id,
    String studentId,
    String firstname,
    String lastname,
    String department,
    String year,
    String program
) {}
