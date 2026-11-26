package com.coelhotechne.detection_system.sentinelRegistry.api.dto.link;

import com.coelhotechne.detection_system.sentinelRegistry.api.dto.RegisteredDrone;
import com.coelhotechne.detection_system.sentinelRegistry.domain.enums.DroneControlType;
import com.coelhotechne.detection_system.sentinelRegistry.domain.enums.DronePurpose;
import com.coelhotechne.detection_system.sentinelRegistry.domain.enums.DroneType;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.Instant;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record CoupledDroneResponse(
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
        Instant coupledAt
) {
    public static CoupledDroneResponse of(RegisteredDrone drone){
        return new CoupledDroneResponse(
                drone.droneId(),
                drone.droneName(),
                drone.serialNumber(),
                drone.droneType(),
                drone.dronePurpose(),
                drone.controlType(),
                drone.communicationProtocol(),
                drone.firmwareVersion(),
                drone.maxFlightTimeMinutes(),
                drone.maxSpeedKmh(),
                drone.maxAltitudeMeters(),
                drone.maxRangeMeters(),
                drone.maxPayloadGrams(),
                drone.active(),
                drone.controllerNodeId(),
                drone.coupledAt()
        );
    }
}
