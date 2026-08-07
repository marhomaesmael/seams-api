package com.seams.backend.web.controller;

import com.seams.backend.core.model.*;
import com.seams.backend.core.repository.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
public class AcademicController {

    private final DepartmentRepository departmentRepository;
    private final ProgramRepository programRepository;
    private final YearLevelRepository yearLevelRepository;
    private final ConnectedNodeRepository nodeRepository;

    public AcademicController(DepartmentRepository departmentRepository, ProgramRepository programRepository, 
                              YearLevelRepository yearLevelRepository, ConnectedNodeRepository nodeRepository) {
        this.departmentRepository = departmentRepository;
        this.programRepository = programRepository;
        this.yearLevelRepository = yearLevelRepository;
        this.nodeRepository = nodeRepository;
    }

    @GetMapping("/departments")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public List<Department> getDepartments() {
        return departmentRepository.findAll();
    }

    @GetMapping("/programs")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public List<Program> getPrograms() {
        return programRepository.findAll();
    }

    @GetMapping("/year-levels")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public List<YearLevel> getYearLevels() {
        return yearLevelRepository.findAll();
    }

    @GetMapping("/nodes")
    @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPERVISOR')")
    public List<ConnectedNode> getNodes() {
        return nodeRepository.findAll();
    }
}
