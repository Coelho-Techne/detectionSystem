package com.coelhotechne.detection_system.sentinelRegistry.infrastructure;

import com.coelhotechne.detection_system.sentinelRegistry.domain.Drone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface DroneRepository extends JpaRepository<Drone, UUID> {

    boolean existsBySerialNumber(String serialNumber);

    List<Drone>findByControllerNodeIdOrderByDroneName(UUID controllerNodeId);

    @Modifying(flushAutomatically = true)
    @Query("update Device d set d.lastCommunication = :at where d.uuid = :id")
    int touchLastCommunication(@Param("id")UUID droneId, @Param("at")LocalDateTime at);
}
