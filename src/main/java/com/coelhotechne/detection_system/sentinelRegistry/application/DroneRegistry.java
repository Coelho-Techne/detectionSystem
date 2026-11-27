package com.coelhotechne.detection_system.sentinelRegistry.application;

import com.coelhotechne.detection_system.sentinelRegistry.api.dto.*;
import com.coelhotechne.detection_system.sentinelRegistry.domain.Drone;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface DroneRegistry {

    Optional<RegisteredDrone>findDrone(UUID droneId);
    DroneCreatedResponse registerDrone(DroneRegisterRequest request);
    Page<DroneResponse> listPage(Pageable pageable);
    DroneResponse getDrone(UUID droneId);
    DroneResponse updateDrone(UUID droneId, DroneUpdateRequest request);
    void deleteDrone(UUID droneId);

    RegisteredDrone requireActive(UUID droneId);
    Drone requireDrone(UUID droneId);
}
