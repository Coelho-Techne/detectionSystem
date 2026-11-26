package com.coelhotechne.detection_system.sentinelRegistry.api.dto.link;

import com.coelhotechne.detection_system.sentinelRegistry.domain.DroneFlightEvent;
import com.coelhotechne.detection_system.sentinelRegistry.domain.enums.DroneEventSeverity;
import com.coelhotechne.detection_system.sentinelRegistry.domain.enums.DroneEventType;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.Instant;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record DroneFlightEventResponse (
        UUID eventId,
        UUID controllerNodeId,
        DroneEventType eventType,
        DroneEventSeverity severity,
        Instant occurredAt,
        Instant receivedAt,
        String missionId,
        String command,
        String message,
        Double latitude,
        Double longitude
){
    public static DroneFlightEventResponse of(DroneFlightEvent event){
        return new DroneFlightEventResponse(
                event.getEventId(),
                event.getControllerNodeId(),
                event.getEventType(),
                event.getSeverity(),
                event.getOccurredAt(),
                event.getReceivedAt(),
                event.getMissionId(),
                event.getCommand(),
                event.getMessage(),
                event.getLatitude(),
                event.getLongitude()
        );
    }
}
