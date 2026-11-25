package com.coelhotechne.detection_system.sentinelRegistry.api.dto;

import com.coelhotechne.detection_system.sentinelRegistry.domain.enums.DroneControlType;
import com.coelhotechne.detection_system.sentinelRegistry.domain.enums.DronePurpose;
import com.coelhotechne.detection_system.sentinelRegistry.domain.enums.DroneType;
import jakarta.validation.constraints.*;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record DroneUpdateRequest(
        @NotBlank @Size(max = 60)
        String droneName,
        @Size(max = 60)
        String manufacturer,
        @Size(max = 60)
        String model,
        @NotNull
        DroneType droneType,
        @NotNull
        DronePurpose dronePurpose,
        @NotNull
        DroneControlType controlType,
        @Positive
        Integer weightGrams,
        @PositiveOrZero
        Integer maxPayloadGrams,
        @Positive
        Integer maxFlightTimeMinutes,
        @Positive
        Double maxSpeedKmh,
        @Positive
        Double maxAltitudeMeters,
        @Positive
        Double maxRangeMeters,
        @Size(max = 30)
        String firmwareVersion,
        @Size(max = 50) @Pattern(regexp = DroneConstraints.PROTOCOL_PATTERN)
        String communicationProtocol,
        @PastOrPresent
        LocalDate manufacturedAt,
        @PastOrPresent
        LocalDateTime lastMaintenanceAt,
        @Size(max = 64)
        String ipAddress,
        Boolean status
) implements DroneData{
}
