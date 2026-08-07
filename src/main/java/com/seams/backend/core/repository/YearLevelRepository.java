package com.seams.backend.core.repository;

import com.seams.backend.core.model.YearLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface YearLevelRepository extends JpaRepository<YearLevel, Integer> {
    Optional<YearLevel> findByLevel(String level);
}
