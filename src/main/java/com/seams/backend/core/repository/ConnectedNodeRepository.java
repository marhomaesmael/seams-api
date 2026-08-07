package com.seams.backend.core.repository;

import com.seams.backend.core.model.ConnectedNode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ConnectedNodeRepository extends JpaRepository<ConnectedNode, Integer> {
    Optional<ConnectedNode> findByToken(String token);
    Optional<ConnectedNode> findByNameAndDeptCode(String name, String deptCode);
}
