package com.coelhotechne.detection_system.sentinelRegistry.api.dto;

import com.coelhotechne.detection_system.sentinelRegistry.domain.enums.DroneControlType;
import com.coelhotechne.detection_system.sentinelRegistry.domain.enums.DronePurpose;
import com.coelhotechne.detection_system.sentinelRegistry.domain.enums.DroneType;

import java.util.UUID;

public record RegisteredDrone(
        UUID droneId,
        String droneName,
        String serialNumber,
        DroneType droneType,
        DronePurpose dronePurpose,
        DroneControlType controlType,
        String communicationProtocol,
        String firmwareVersion,
        Integer maxFlightTimeMinutes,
        Double maxSpeedKmh,
        Double maxAltitudeMeters,
        Double maxRangeMeters,
        Integer maxPayloadGrams,
        boolean active,
        UUID controllerNodeId,
        java.time.Instant coupledAt
) {
}
