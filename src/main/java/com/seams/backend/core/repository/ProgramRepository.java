package com.seams.backend.core.repository;

import com.seams.backend.core.model.Program;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProgramRepository extends JpaRepository<Program, Integer> {
    Optional<Program> findByCodeIgnoreCase(String code);
    Optional<Program> findByNameIgnoreCase(String name);
}
