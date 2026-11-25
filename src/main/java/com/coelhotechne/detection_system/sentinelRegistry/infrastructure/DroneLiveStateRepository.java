package com.coelhotechne.detection_system.sentinelRegistry.infrastructure;

import com.coelhotechne.detection_system.sentinelRegistry.domain.DroneLiveState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DroneLiveStateRepository extends JpaRepository<DroneLiveState, UUID> {
}
