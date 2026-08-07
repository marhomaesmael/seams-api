package com.seams.backend.application.dto;

import com.seams.backend.core.model.AttendanceRecord;
import java.util.List;

public record EventUploadRequest(
    String eventName,
    String eventDate,
    String aseadoProfile,
    String localSyncId,
    boolean hasLogout,
    String filterJson,
    List<AttendanceRecord> records
) {}
