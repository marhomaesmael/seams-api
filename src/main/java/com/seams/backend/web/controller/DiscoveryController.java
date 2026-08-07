package com.seams.backend.web.controller;

import com.seams.backend.core.model.*;
import com.seams.backend.core.repository.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/auth/discovery")
public class DiscoveryController {

    private final DepartmentRepository departmentRepository;
    private final ProgramRepository programRepository;
    private final YearLevelRepository yearLevelRepository;

    public DiscoveryController(DepartmentRepository departmentRepository, 
                               ProgramRepository programRepository, 
                               YearLevelRepository yearLevelRepository) {
        this.departmentRepository = departmentRepository;
        this.programRepository = programRepository;
        this.yearLevelRepository = yearLevelRepository;
    }

    @GetMapping("/departments")
    public List<Department> getDepartments() {
        return departmentRepository.findAll().stream()
                .sorted(Comparator.comparing(Department::getCode, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    @GetMapping("/programs")
    public List<Program> getPrograms(@RequestParam(required = false) String deptCode) {
        List<Program> all = programRepository.findAll();
        if (deptCode != null && !deptCode.isBlank()) {
            return all.stream()
                    .filter(p -> p.getDepartment() != null && p.getDepartment().getCode().equals(deptCode))
                    .sorted(Comparator.comparing(Program::getCode, String.CASE_INSENSITIVE_ORDER))
                    .collect(Collectors.toList());
        }
        return all.stream()
                .sorted(Comparator.comparing(Program::getCode, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    @GetMapping("/year-levels")
    public List<YearLevel> getYearLevels() {
        return yearLevelRepository.findAll().stream()
                .sorted(Comparator.comparing(YearLevel::getLevel))
                .collect(Collectors.toList());
    }
}
