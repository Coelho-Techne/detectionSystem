package com.coelhotechne.detection_system.sentinelRegistry.api.dto.link;

import com.coelhotechne.detection_system.sentinelRegistry.domain.enums.DroneEventSeverity;
import com.coelhotechne.detection_system.sentinelRegistry.domain.enums.DroneEventType;
import jakarta.validation.constraints.*;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.Instant;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record DroneFlightEventItem(
        @NotNull
        UUID eventId,
        @NotNull
        DroneEventType eventType,
        DroneEventSeverity severity,
        @NotNull
        Instant occurredAt,
        @Size(max = 64)
        String missionId,
        @Size(max = 40) @Pattern(regexp = "^[A-Z0-9_]+$")
        String command,
        @Size(max = 500)
        String message,
        @DecimalMin("-90.0") @DecimalMax("90.0")
        Double latitude,
        @DecimalMin("-180.0") @DecimalMax("180.0")
        Double longitude
) {
}
