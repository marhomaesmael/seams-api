package com.seams.backend.application.service;

import com.seams.backend.core.model.Event;
import com.seams.backend.core.model.AttendanceRecord;
import com.seams.backend.core.model.Student;
import com.seams.backend.core.repository.EventRepository;
import com.seams.backend.core.repository.AttendanceRecordRepository;
import com.seams.backend.core.repository.StudentRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class EventService {

    private final EventRepository repository;
    private final AttendanceRecordRepository attendanceRepository;
    private final StudentRepository studentRepository;

    public EventService(EventRepository repository, AttendanceRecordRepository attendanceRepository, StudentRepository studentRepository) {
        this.repository = repository;
        this.attendanceRepository = attendanceRepository;
        this.studentRepository = studentRepository;
    }

    @Cacheable(value = "events", key = "'pending'")
    public List<Event> findPendingApprovals() {
        return repository.findAll().stream()
                .filter(e -> "PENDING".equals(e.getStatus()))
                .toList();
    }

    @Cacheable(value = "events", key = "'accepted'")
    public List<Event> findAcceptedEvents() {
        return repository.findAll().stream()
                .filter(e -> "ACCEPTED".equals(e.getStatus()))
                .toList();
    }

    @Cacheable(value = "events", key = "#id")
    public Optional<Event> findById(Integer id) {
        return repository.findById(id);
    }

    public List<Map<String, Object>> findRecordsWithNames(Integer eventId) {
        List<AttendanceRecord> records = attendanceRepository.findAll().stream()
                .filter(r -> eventId.equals(r.getEventId()))
                .toList();
        
        return records.stream().map(r -> {
            Optional<Student> student = studentRepository.findByStudentId(r.getStudentId());
            Map<String, Object> map = new HashMap<>();
            map.put("studentId", r.getStudentId());
            map.put("studentName", student.map(s -> s.getFirstname() + " " + s.getLastname()).orElse("Unknown"));
            map.put("loginTime", r.getTimeIn());
            map.put("logoutTime", r.getTimeOut());
            map.put("isLate", r.isLate());
            map.put("status", r.getStatus());
            return map;
        }).collect(Collectors.toList());
    }

    @Transactional
    @CacheEvict(value = {"events", "students"}, allEntries = true)
    public Event updateStatus(Integer id, String status) {
        Event event = repository.findById(id).orElseThrow();
        
        if ("ACCEPTED".equals(status)) {
            // If this was an update (detected by name or a better flag), replace the original
            if (event.getName().endsWith(" (Update)")) {
                String originalName = event.getName().replace(" (Update)", "");
                repository.findAll().stream()
                        .filter(e -> originalName.equals(e.getName()) && 
                                    event.getLocalSyncId().equals(e.getLocalSyncId()) &&
                                    event.getAseadoProfile().equals(e.getAseadoProfile()) &&
                                    "ACCEPTED".equals(e.getStatus()) &&
                                    !e.getId().equals(event.getId()))
                        .findFirst()
                        .ifPresent(old -> {
                            attendanceRepository.deleteByEventId(old.getId());
                            repository.delete(old);
                        });
                event.setName(originalName);
            }
        }
        
        event.setStatus(status);
        return repository.save(event);
    }

    @Transactional
    @CacheEvict(value = {"events", "students"}, allEntries = true)
    public void deleteAcceptedEvent(Integer eventId) {
        Event event = repository.findById(eventId).orElseThrow();
        if ("ACCEPTED".equals(event.getStatus())) {
            attendanceRepository.deleteByEventId(event.getId());
            repository.delete(event);
        } else {
            throw new RuntimeException("Only accepted events can be removed from history.");
        }
    }
}
