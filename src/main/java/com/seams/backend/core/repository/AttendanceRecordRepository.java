package com.seams.backend.core.repository;

import com.seams.backend.core.model.AttendanceRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Integer> {
    List<AttendanceRecord> findByStudentId(String studentId);
    List<AttendanceRecord> findByEventId(Integer eventId);
    Page<AttendanceRecord> findByEventId(Integer eventId, Pageable pageable);
    Page<AttendanceRecord> findByEventIdAndStudentIdIn(Integer eventId, List<String> studentIds, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(r) FROM AttendanceRecord r WHERE r.eventId = :eventId")
    long countByEventId(@org.springframework.data.repository.query.Param("eventId") Integer eventId);

    @Transactional
    @Modifying
    void deleteByStudentId(String studentId);

    @Transactional
    @Modifying
    void deleteByEventId(Integer eventId);
}
