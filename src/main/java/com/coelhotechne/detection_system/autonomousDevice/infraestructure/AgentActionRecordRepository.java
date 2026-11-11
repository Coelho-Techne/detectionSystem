package com.coelhotechne.detection_system.autonomousDevice.infraestructure;

import com.coelhotechne.detection_system.autonomousDevice.domain.AgentActionRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AgentActionRecordRepository extends JpaRepository<AgentActionRecord, UUID> {
    Page<AgentActionRecord>findByAgentUuid(UUID agentUuid, Pageable pageable);
}
