package com.seams.backend.application.service;

import com.seams.backend.infrastructure.security.JwtService;
import com.seams.backend.application.dto.EventUploadRequest;
import com.seams.backend.core.model.*;
import com.seams.backend.core.repository.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
public class SyncService {

    private final AttendanceRecordRepository attendanceRepository;
    private final EventRepository eventRepository;
    private final SystemSettingRepository settingRepository;
    private final DepartmentRepository departmentRepository;
    private final ConnectedNodeRepository nodeRepository;
    private final JwtService jwtService;
    private final org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate;

    private static final String PAIRING_KEY = "PAIRING_KEY";

    public SyncService(AttendanceRecordRepository attendanceRepository, 
                       EventRepository eventRepository,
                       SystemSettingRepository settingRepository,
                       DepartmentRepository departmentRepository,
                       ConnectedNodeRepository nodeRepository,
                       JwtService jwtService,
                       org.springframework.messaging.simp.SimpMessagingTemplate messagingTemplate) {
        this.attendanceRepository = attendanceRepository;
        this.eventRepository = eventRepository;
        this.settingRepository = settingRepository;
        this.departmentRepository = departmentRepository;
        this.nodeRepository = nodeRepository;
        this.jwtService = jwtService;
        this.messagingTemplate = messagingTemplate;
    }

    public String getCurrentPairingKey() {
        return settingRepository.findById(PAIRING_KEY)
                .map(SystemSetting::getValue)
                .orElseGet(this::generatePairingKey);
    }

    public String generatePairingKey() {
        String key = String.format("%06d", new Random().nextInt(1000000));
        settingRepository.save(new SystemSetting(PAIRING_KEY, key));
        return key;
    }

    public Map<String, Object> requestPairing(String pairingKey, String name, String deptCode, String deptName) {
        // Rate limit: 1 minute between attempts for the same node identity
        nodeRepository.findByNameAndDeptCode(name, deptCode).ifPresent(node -> {
            if (node.getLastHeartbeat() != null && 
                Instant.now().isBefore(node.getLastHeartbeat().plusSeconds(60))) {
                throw new RuntimeException("Security Protocol: Rate limit active. Please wait 60 seconds between pairing attempts.");
            }
        });

        String currentKey = getCurrentPairingKey();
        if (!currentKey.equals(pairingKey)) {
            throw new RuntimeException("Invalid pairing key");
        }

        // UNIFIED MATCHING: Standardize on Full Name (deptName). 
        // Match against existing Code OR Name (Case Insensitive).
        Department dept = departmentRepository.findByCodeIgnoreCase(deptName)
                .or(() -> departmentRepository.findByNameIgnoreCase(deptName))
                .orElseGet(() -> departmentRepository.save(new Department(deptCode, deptName))); 
    
        // Create or Update PENDING Request
        ConnectedNode node = nodeRepository.findByNameAndDeptCode(name, deptCode)
                .orElse(new ConnectedNode());
        node.setName(name);
        node.setDeptCode(deptCode);
        node.setStatus("PENDING");
        node.setLastHeartbeat(Instant.now());
        ConnectedNode saved = nodeRepository.save(node);

        return Map.of("requestId", saved.getId(), "status", "PENDING");
    }

    public ConnectedNode approveNode(Integer id) {
        ConnectedNode node = nodeRepository.findById(id).orElseThrow();
        
        var userDetails = new User("aseado-node", "", Collections.singletonList(new SimpleGrantedAuthority("ADMIN")));
        String token = jwtService.generateToken(userDetails);
        
        node.setToken(token);
        node.setStatus("ACTIVE");
        node.setLastHeartbeat(Instant.now());
        return nodeRepository.save(node);
    }

    public void rejectNode(Integer id) {
        nodeRepository.deleteById(id);
    }

    public void terminateNode(Integer id) {
        ConnectedNode node = nodeRepository.findById(id).orElseThrow();
        String oldToken = node.getToken();
        node.setStatus("REVOKED");
        node.setToken(null);
        nodeRepository.save(node);
        
        // Notify the node via WebSocket using its unique token as the channel identifier
        if (oldToken != null) {
            messagingTemplate.convertAndSend("/topic/node/" + oldToken, 
                Map.of("status", "REVOKED", "message", "Institutional access revoked by Hub Administrator."));
        }
    }

    public Map<String, Object> getPairingStatus(Integer requestId) {
        ConnectedNode node = nodeRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Security Protocol: Pairing Request ID '" + requestId + "' not found. Re-pairing required."));
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("status", node.getStatus());
        if ("ACTIVE".equals(node.getStatus())) {
            response.put("token", node.getToken());
        }
        return response;
    }

    @Transactional
    @CacheEvict(value = {"events", "students"}, allEntries = true)
    public void syncAttendance(EventUploadRequest request) {
        List<Event> existing = eventRepository.findByLocalSyncIdAndAseadoProfile(request.localSyncId(), request.aseadoProfile());
        
        // Logic: 
        // 1. If a PENDING update exists, update it.
        // 2. If only an ACCEPTED one exists, create a PENDING update clone.
        // 3. If nothing exists, create a new PENDING event.
        
        Event event;
        Optional<Event> pendingEvent = existing.stream()
                .filter(e -> "PENDING".equals(e.getStatus()))
                .findFirst();

        if (pendingEvent.isPresent()) {
            event = pendingEvent.get();
        } else {
            Optional<Event> acceptedEvent = existing.stream()
                    .filter(e -> "ACCEPTED".equals(e.getStatus()))
                    .findFirst();

            event = new Event();
            if (acceptedEvent.isPresent()) {
                event.setName(request.eventName() + " (Update)");
            } else {
                event.setName(request.eventName());
            }
        }

        event.setEventDate(request.eventDate());
        event.setAseadoProfile(request.aseadoProfile());
        event.setStatus("PENDING");
        event.setLocalSyncId(request.localSyncId());
        event.setHasLogout(request.hasLogout());
        event.setFilterJson(request.filterJson());
        
        Event savedEvent = eventRepository.save(event);
        
        // Clear old records for THIS specific PENDING event before re-adding
        attendanceRepository.deleteByEventId(savedEvent.getId());
        
        List<AttendanceRecord> records = request.records();
        for (AttendanceRecord record : records) {
            record.setEventId(savedEvent.getId());
            record.setAseadoProfile(request.aseadoProfile());
            record.setCreatedAt(Instant.now());
        }
        attendanceRepository.saveAll(records);
    }

    @Transactional
    @CacheEvict(value = {"events", "students"}, allEntries = true)
    public void cancelUpload(String localSyncId, String aseadoProfile) {
        List<Event> existing = eventRepository.findByLocalSyncIdAndAseadoProfile(localSyncId, aseadoProfile);
        existing.stream()
                .filter(e -> "PENDING".equals(e.getStatus()))
                .forEach(event -> {
                    attendanceRepository.deleteByEventId(event.getId());
                    eventRepository.delete(event);
                });
    }

    public Optional<Event> getEventByLocalId(String localSyncId, String aseadoProfile) {
        List<Event> existing = eventRepository.findByLocalSyncIdAndAseadoProfile(localSyncId, aseadoProfile);
        // Prioritize PENDING status for the desktop poll
        return existing.stream()
                .filter(e -> "PENDING".equals(e.getStatus()))
                .findFirst()
                .or(() -> existing.stream().filter(e -> "ACCEPTED".equals(e.getStatus())).findFirst());
    }
}
