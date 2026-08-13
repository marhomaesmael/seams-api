package com.seams.backend.web.controller;

import com.seams.backend.application.dto.StudentResponse;
import com.seams.backend.core.model.Student;
import com.seams.backend.application.service.StudentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/admin/students")
public class StudentController {

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public Page<StudentResponse> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String department,
            Pageable pageable) {
        return service.findAll(search, department, pageable)
                .map(this::mapToResponse);
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public StudentResponse create(@RequestBody Student student) {
        return mapToResponse(service.save(student));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public StudentResponse update(@PathVariable Integer id, @RequestBody Student student) {
        return mapToResponse(service.update(id, student));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public void delete(@PathVariable Integer id) {
        service.deleteById(id);
    }

    @PostMapping("/import")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public ResponseEntity<String> importCsv(@RequestParam("file") MultipartFile file) {
        try {
            String result = service.importFromCsv(file);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Import failed: " + e.getMessage());
        }
    }

    private StudentResponse mapToResponse(Student s) {
        return new StudentResponse(
                s.getId(),
                s.getStudentId(),
                s.getFirstname(),
                s.getLastname(),
                s.getMiddlename(),
                s.getSuffix(),
                s.getRecoveryCode(),
                s.getDepartment(),
                s.getProgram(),
                s.getYear(),
                s.getEnrollments() == null ? List.of() : s.getEnrollments().stream()
                        .map(e -> new StudentResponse.EnrollmentDto(
                                e.getAcademicYear(),
                                e.getSemester(),
                                e.getDepartment().getName(),
                                e.getProgram().getName(),
                                e.getYearLevel().getLevel()
                        )).collect(Collectors.toList())
        );
    }
}
