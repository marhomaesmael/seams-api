package com.seams.backend.application.service;

import com.opencsv.CSVReader;
import com.seams.backend.core.model.*;
import com.seams.backend.core.repository.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.util.*;

@Service
public class StudentService {

    private final StudentRepository repository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    private final DepartmentRepository departmentRepository;
    private final ProgramRepository programRepository;
    private final YearLevelRepository yearLevelRepository;
    private final StudentEnrollmentRepository enrollmentRepository;
    private final AttendanceRecordRepository attendanceRepository;

    public StudentService(StudentRepository repository, UserRepository userRepository, 
                          PasswordEncoder passwordEncoder, DepartmentRepository departmentRepository,
                          ProgramRepository programRepository, YearLevelRepository yearLevelRepository,
                          StudentEnrollmentRepository enrollmentRepository,
                          AttendanceRecordRepository attendanceRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.departmentRepository = departmentRepository;
        this.programRepository = programRepository;
        this.yearLevelRepository = yearLevelRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "students", key = "(#search != null ? #search : 'all') + '_' + (#department != null ? #department : 'all') + '_' + #pageable.pageNumber + '_' + #pageable.pageSize")
    public Page<Student> findAll(String search, String department, Pageable pageable) {
        if (department != null && !department.isBlank()) {
            return repository.findAllByDepartment(department, pageable);
        }
        if (search != null && !search.isBlank()) {
            return repository.search(search, pageable);
        }
        return repository.findAllWithEnrollments(pageable);
    }

    public Optional<Student> findById(Integer id) {
        return repository.findById(id);
    }

    @Transactional
    @CacheEvict(value = {"students", "stats"}, allEntries = true)
    public Student save(Student student) {
        Optional<Student> existing = repository.findByStudentId(student.getStudentId());
        if (existing.isPresent()) {
            return update(existing.get().getId(), student);
        }

        if (student.getUser() == null) {
            User user = User.builder()
                    .username(student.getStudentId())
                    .password(passwordEncoder.encode(student.getLastname().toUpperCase()))
                    .displayName(student.getFirstname() + " " + student.getLastname())
                    .role(Role.STUDENT)
                    .mustChangePassword(true)
                    .build();
            student.setUser(userRepository.save(user));
        }
        
        if (student.getRecoveryCode() == null) {
            student.setRecoveryCode(generateRecoveryCode());
        }
        
        Student saved = repository.save(student);

        // V2: Initialize current enrollment if provided in legacy fields or explicitly
        if (student.getDepartment() != null && student.getProgram() != null) {
            ensureEnrollment(saved, student.getDepartment(), student.getProgram(), student.getYear());
        }

        return saved;
    }

    @Transactional
    @CacheEvict(value = {"students", "stats"}, allEntries = true)
    public Student update(Integer id, Student request) {
        Student student = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Student not found"));
        
        student.setStudentId(request.getStudentId());
        student.setLastname(request.getLastname());
        student.setFirstname(request.getFirstname());
        student.setMiddlename(request.getMiddlename());
        student.setSuffix(request.getSuffix());
        
        // Identity Consolidation: Store current state in legacy fields for quick search/display
        student.setDepartment(request.getDepartment());
        student.setProgram(request.getProgram());
        student.setYear(request.getYear());

        if (student.getUser() != null) {
            User user = student.getUser();
            user.setUsername(request.getStudentId());
            user.setDisplayName(request.getFirstname() + " " + request.getLastname());
            userRepository.save(user);
        }

        // Update enrollment if dept/prog changed
        if (request.getDepartment() != null && request.getProgram() != null) {
             ensureEnrollment(student, request.getDepartment(), request.getProgram(), request.getYear());
        }

        return repository.save(student);
    }

    @Transactional
    @CacheEvict(value = {"students", "stats"}, allEntries = true)
    public void deleteById(Integer id) {
        Student student = repository.findById(id).orElse(null);
        if (student != null) {
            // Manual Cascade Cleanup
            enrollmentRepository.deleteByStudent(student);
            attendanceRepository.deleteByStudentId(student.getStudentId());
            
            User user = student.getUser();
            repository.delete(student);
            if (user != null) {
                userRepository.delete(user);
            }
        }
    }

    private String generateRecoveryCode() {
        return String.format("%08d", new Random().nextInt(100000000));
    }

    @Transactional
    @CacheEvict(value = {"students", "stats"}, allEntries = true)
    public String importFromCsv(MultipartFile file) throws Exception {
        List<String[]> dataLines = new ArrayList<>();
        Map<String, Integer> colMap = new HashMap<>();
        
        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
            String[] headers = reader.readNext();
            if (headers == null) return "Import failed: CSV file is empty.";

            for (int i = 0; i < headers.length; i++) {
                String h = headers[i].toLowerCase().trim().replace(" ", "").replace("_", "");
                colMap.put(h, i);
            }

            boolean hasId = colMap.containsKey("id") || colMap.containsKey("studentid");
            boolean hasLast = colMap.containsKey("lastname") || colMap.containsKey("surname");
            boolean hasFirst = colMap.containsKey("firstname");
            boolean hasYear = colMap.containsKey("year") || colMap.containsKey("yearlevel");
            boolean hasProg = colMap.containsKey("program") || colMap.containsKey("course");
            boolean hasDept = colMap.containsKey("department") || colMap.containsKey("dept");

            if (!hasId || !hasLast || !hasFirst || !hasYear || !hasProg || !hasDept) {
                throw new RuntimeException("Import Rejected: Missing mandatory columns (ID, Lastname, Firstname, Year, Program, Department required).");
            }

            String[] line;
            while ((line = reader.readNext()) != null) {
                dataLines.add(line);
            }
        }

        if (dataLines.isEmpty()) return "Processed 0 students.";

        Set<String> departmentsInFile = new HashSet<>();
        for (String[] line : dataLines) {
            String d = getVal(line, colMap, "department", "dept");
            if (!d.isBlank()) departmentsInFile.add(d);
        }

        // Delete existing enrollments for departments found in the file to avoid duplicates
        for (String deptCode : departmentsInFile) {
            departmentRepository.findByCodeIgnoreCase(deptCode).ifPresent(enrollmentRepository::deleteByDepartment);
        }

        Map<String, String> passwordCache = new HashMap<>(); 
        Map<String, Student> studentCache = new HashMap<>(); 
        Map<String, Department> deptCache = new HashMap<>(); 
        Map<String, Program> progCache = new HashMap<>(); 
        Map<String, YearLevel> yearCache = new HashMap<>(); 

        repository.findAll().forEach(s -> studentCache.put(s.getStudentId(), s));
        departmentRepository.findAll().forEach(d -> deptCache.put(d.getCode(), d));
        programRepository.findAll().forEach(p -> progCache.put(p.getCode(), p));
        yearLevelRepository.findAll().forEach(y -> yearCache.put(y.getLevel(), y));

        int processedCount = 0;
        int updatedCount = 0;
        int createdCount = 0;

        for (String[] line : dataLines) {
            String sid = getVal(line, colMap, "studentid", "id");
            String ln = getVal(line, colMap, "lastname", "surname");
            String fn = getVal(line, colMap, "firstname");
            
            if (sid.isBlank() || ln.isBlank() || fn.isBlank()) continue;

            Student student = studentCache.get(sid);
            if (student != null) {
                student.setLastname(ln);
                student.setFirstname(fn);
                student.setMiddlename(getVal(line, colMap, "middlename", "middle"));
                student.setSuffix(getVal(line, colMap, "suffix"));
                if (student.getUser() != null) {
                    student.getUser().setDisplayName(fn + " " + ln);
                }
                updatedCount++;
            } else {
                student = new Student();
                student.setStudentId(sid);
                student.setLastname(ln);
                student.setFirstname(fn);
                student.setMiddlename(getVal(line, colMap, "middlename", "middle"));
                student.setSuffix(getVal(line, colMap, "suffix"));
                student.setRecoveryCode(generateRecoveryCode());
                
                String pass = ln.toUpperCase();
                String hashed = passwordCache.computeIfAbsent(pass, passwordEncoder::encode);
                
                User user = User.builder()
                        .username(sid)
                        .password(hashed)
                        .displayName(fn + " " + ln)
                        .role(Role.STUDENT)
                        .mustChangePassword(true)
                        .build();
                student.setUser(userRepository.save(user));
                createdCount++;
            }

            String dCode = getVal(line, colMap, "department", "dept");
            String pCode = getVal(line, colMap, "program", "course");
            String yLvl = getVal(line, colMap, "year", "yearlevel", "level");

            student.setYear(yLvl);

            Student saved = repository.save(student);
            studentCache.put(sid, saved);

            if (!dCode.isBlank() && !pCode.isBlank()) {
                ensureEnrollment(saved, dCode, pCode, yLvl);
            }
            processedCount++;
        }

        return String.format("V2 High-Performance Sync: %d students processed (%d created, %d updated) across %d departments.", 
                processedCount, createdCount, updatedCount, departmentsInFile.size());
    }

    private void ensureEnrollment(Student student, String deptValue, String progValue, String yearLevel) {
        // Resolve Department: Match the input value against BOTH Code and Name (Case Insensitive)
        Department dept = departmentRepository.findByCodeIgnoreCase(deptValue)
                .or(() -> departmentRepository.findByNameIgnoreCase(deptValue))
                .orElseGet(() -> departmentRepository.save(new Department(deptValue, deptValue)));
        
        // Resolve Program: Match the input value against BOTH Code and Name (Case Insensitive)
        Program prog = programRepository.findByCodeIgnoreCase(progValue)
                .or(() -> programRepository.findByNameIgnoreCase(progValue))
                .orElseGet(() -> programRepository.save(new Program(progValue, progValue, dept)));

        // Canonicalization: Force the student profile to use the official Hub names (the long one)
        student.setDepartment(dept.getName());
        student.setProgram(prog.getName());
        student.setYear(yearLevel);

        YearLevel yl = yearLevelRepository.findByLevel(yearLevel)
                .orElseGet(() -> yearLevelRepository.save(new YearLevel(yearLevel)));

        // Check if enrollment already exists for this semester
        boolean exists = enrollmentRepository.findByStudent(student).stream()
                .anyMatch(e -> e.getAcademicYear().equals("2026-2027") && e.getSemester().equals("1st Semester") && e.getDepartment().getCode().equals(dept.getCode()));
        
        if (!exists) {
            StudentEnrollment enrollment = new StudentEnrollment();
            enrollment.setStudent(student);
            enrollment.setDepartment(dept);
            enrollment.setProgram(prog);
            enrollment.setYearLevel(yl);
            enrollment.setAcademicYear("2026-2027"); 
            enrollment.setSemester("1st Semester");
            enrollmentRepository.save(enrollment);
        }
    }

    private String getVal(String[] line, Map<String, Integer> colMap, String... keys) {
        for (String k : keys) {
            Integer idx = colMap.get(k);
            if (idx != null && idx < line.length) return line[idx].trim();
        }
        return "";
    }
}
