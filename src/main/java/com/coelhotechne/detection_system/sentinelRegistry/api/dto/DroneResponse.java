package com.coelhotechne.detection_system.sentinelRegistry.api.dto;

import com.coelhotechne.detection_system.batterysupply.domain.PowerSupply;
import com.coelhotechne.detection_system.sentinelRegistry.domain.enums.DroneControlType;
import com.coelhotechne.detection_system.sentinelRegistry.domain.enums.DronePurpose;
import com.coelhotechne.detection_system.sentinelRegistry.domain.enums.DroneType;
import com.fasterxml.jackson.annotation.JsonFormat;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record DroneResponse (
        UUID uuid,
        Long version,
        String createdBy,
        String lastModifiedBy,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
        LocalDateTime updatedAt,
        String droneName,
        String serialNumber,
        String manufacturer,
        String model,
        DroneType droneType,
        DronePurpose dronePurpose,
        DroneControlType controlType,
        Integer weightGrams,
        Integer maxPayloadGrams,
        Integer maxFlightTimeMinutes,
        Double maxSpeedKmh,
        Double maxAltitudeMeters,
        Double maxRangeMeters,
        String firmwareVersion,
        String communicationProtocol,
        LocalDate manufacturedAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
        LocalDateTime lastMaintenanceAt,
        String ipAddress,
        Boolean status,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy HH:mm:ss")
        LocalDateTime lastCommunication,
        PowerSupply powerSupply,
        UUID controllerNodeId,
        java.time.Instant coupledAt
){
}
