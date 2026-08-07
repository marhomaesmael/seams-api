package com.seams.backend.core.repository;

import com.seams.backend.core.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Integer> {
    List<Event> findByLocalSyncIdAndAseadoProfile(String localSyncId, String aseadoProfile);
}
