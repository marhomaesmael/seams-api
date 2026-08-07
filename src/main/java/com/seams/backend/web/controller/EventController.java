package com.seams.backend.web.controller;

import com.seams.backend.core.model.Event;
import com.seams.backend.application.service.EventService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/approvals")
public class EventController {

    private final EventService service;

    public EventController(EventService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public List<Map<String, Object>> getPending() {
        return service.findPendingApprovals().stream()
                .map(e -> {
                    List<Map<String, Object>> logs = service.findRecordsWithNames(e.getId());
                    return Map.of(
                        "id", e.getId().toString(),
                        "eventName", e.getName(),
                        "uploadDate", e.getEventDate(),
                        "hasLogout", e.isHasLogout(),
                        "recordCount", logs.size(),
                        "status", "Pending",
                        "logs", logs
                    );
                })
                .toList();
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public List<Map<String, Object>> getHistory() {
        return service.findAcceptedEvents().stream()
                .map(e -> {
                    List<Map<String, Object>> logs = service.findRecordsWithNames(e.getId());
                    return Map.of(
                        "id", e.getId().toString(),
                        "eventName", e.getName(),
                        "uploadDate", e.getEventDate(),
                        "hasLogout", e.isHasLogout(),
                        "recordCount", logs.size(),
                        "status", "Accepted",
                        "logs", logs
                    );
                })
                .toList();
    }

    @PostMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public void updateStatus(@PathVariable Integer id, @RequestBody Map<String, String> request) {
        String status = request.get("status");
        service.updateStatus(id, "Accepted".equalsIgnoreCase(status) ? "ACCEPTED" : "REJECTED");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPERVISOR')")
    public void deleteEvent(@PathVariable Integer id) {
        // service method deleteAcceptedEvent will handle the check
        service.deleteAcceptedEvent(id);
    }
}
