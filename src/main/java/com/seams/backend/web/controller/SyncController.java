package com.seams.backend.web.controller;

import com.seams.backend.application.dto.EventUploadRequest;
import com.seams.backend.application.dto.StudentSyncDto;
import com.seams.backend.core.model.ConnectedNode;
import com.seams.backend.application.service.SyncService;
import com.seams.backend.core.repository.StudentRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sync")
public class SyncController {

    private final SyncService service;
    private final StudentRepository studentRepository;

    public SyncController(SyncService service, StudentRepository studentRepository) {
        this.service = service;
        this.studentRepository = studentRepository;
    }

    @GetMapping("/pairing-key")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public String getPairingKey() {
        return service.getCurrentPairingKey();
    }

    @PostMapping("/pairing-key/regenerate")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public String regeneratePairingKey() {
        return service.generatePairingKey();
    }

    @PostMapping("/pair")
    public ResponseEntity<Map<String, Object>> requestPairing(@RequestBody Map<String, String> body) {
        String pairingKey = body.get("pairingKey");
        String name = body.get("name");
        String deptCode = body.get("deptCode");
        String deptName = body.get("deptName");
        return ResponseEntity.ok(service.requestPairing(pairingKey, name, deptCode, deptName));
    }

    @GetMapping("/pairing-status/{requestId}")
    public ResponseEntity<Map<String, Object>> getPairingStatus(@PathVariable Integer requestId) {
        return ResponseEntity.ok(service.getPairingStatus(requestId));
    }

    @GetMapping("/students")
    @Cacheable(value = "students", key = "'sync_' + #department")
    public List<StudentSyncDto> getSyncStudents(@RequestParam String department) {
        return studentRepository.findSyncListByDepartment(department);
    }

    @PostMapping("/approve/{id}")
    @PreAuthorize("hasAuthority('SUPERVISOR')")
    public ResponseEntity<ConnectedNode> approveNode(@PathVariable Integer id) {
        return ResponseEntity.ok(service.approveNode(id));
    }

    @PostMapping("/reject/{id}")
    @PreAuthorize("hasAuthority('SUPERVISOR')")
    public ResponseEntity<Void> rejectNode(@PathVariable Integer id) {
        service.rejectNode(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/terminate/{id}")
    @PreAuthorize("hasAuthority('SUPERVISOR')")
    public ResponseEntity<Void> terminateNode(@PathVariable Integer id) {
        service.terminateNode(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/attendance")
    public ResponseEntity<String> uploadAttendance(@RequestBody EventUploadRequest request) {
        service.syncAttendance(request);
        return ResponseEntity.ok("Sync successful");
    }

    @PostMapping("/attendance/cancel")
    public ResponseEntity<Void> cancelAttendance(@RequestBody Map<String, String> body) {
        service.cancelUpload(body.get("localSyncId"), body.get("aseadoProfile"));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/attendance/status")
    public ResponseEntity<Map<String, String>> getAttendanceStatus(
            @RequestParam String localSyncId, 
            @RequestParam String aseadoProfile) {
        return service.getEventByLocalId(localSyncId, aseadoProfile)
                .map(e -> ResponseEntity.ok(Map.of("status", e.getStatus())))
                .orElse(ResponseEntity.notFound().build());
    }
}
