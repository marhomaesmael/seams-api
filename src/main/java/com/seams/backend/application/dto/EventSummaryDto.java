package com.seams.backend.application.dto;

public record EventSummaryDto(
    Integer id,
    String eventName,
    String uploadDate,
    long recordCount,
    boolean hasLogout,
    String status
) {}
