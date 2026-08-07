package com.seams.backend.application.dto;

import java.util.List;

public record StudentResponse(
    Integer id,
    String studentId,
    String firstname,
    String lastname,
    String middlename,
    String suffix,
    String recoveryCode,
    String department,
    String program,
    String year,
    List<EnrollmentDto> enrollments
) {
    public record EnrollmentDto(
        String academicYear,
        String semester,
        String department,
        String program,
        String yearLevel
    ) {}
}
