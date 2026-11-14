package com.coelhotechne.detection_system.autonomousDevice.infrastructure;

import com.coelhotechne.detection_system.autonomousDevice.domain.AutonomousDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AutonomousDeviceRepository extends JpaRepository<AutonomousDevice, UUID> {
    boolean existsByAgentName(String name);
}
