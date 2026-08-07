package com.seams.backend.web.controller;

import com.seams.backend.core.model.Notification;
import com.seams.backend.application.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @GetMapping("/student/notifications")
    public List<Notification> getAll() {
        return service.findAll();
    }

    @PostMapping("/admin/notifications")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<Notification> publish(@RequestBody Notification notification) {
        return ResponseEntity.ok(service.save(notification));
    }
}
