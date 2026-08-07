package com.seams.backend.web.controller;

import com.seams.backend.core.model.ProblemRequest;
import com.seams.backend.core.model.Status;
import com.seams.backend.application.service.ProblemRequestService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ProblemRequestController {

    private final ProblemRequestService service;

    public ProblemRequestController(ProblemRequestService service) {
        this.service = service;
    }

    @GetMapping("/admin/requests")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public List<ProblemRequest> getAll() {
        return service.findAll();
    }

    @PostMapping("/student/requests")
    @PreAuthorize("hasAuthority('STUDENT')")
    public ProblemRequest submit(@RequestBody Map<String, String> request) {
        return service.submitRequest(request.get("details"));
    }

    @PutMapping("/admin/requests/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public ProblemRequest update(@PathVariable Integer id, @RequestParam Status status, @RequestParam String reply) {
        return service.updateStatus(id, status, reply);
    }
}
