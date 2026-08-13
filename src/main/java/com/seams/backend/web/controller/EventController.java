package com.seams.backend.web.controller;

import com.seams.backend.application.dto.EventSummaryDto;
import com.seams.backend.core.model.Event;
import com.seams.backend.application.service.EventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public List<EventSummaryDto> getPending() {
        return service.findPendingSummaries();
    }

    @GetMapping("/history")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public List<EventSummaryDto> getHistory() {
        return service.findAcceptedSummaries();
    }

    @GetMapping("/{id}/details")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public Page<Map<String, Object>> getDetails(
            @PathVariable Integer id,
            @RequestParam(required = false) String search,
            Pageable pageable) {
        return service.findRecordsWithNames(id, search, pageable);
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
