package com.coelhotechne.detection_system.sentinelRegistry.event;

import com.coelhotechne.detection_system.sentinelRegistry.domain.enums.DroneEventSeverity;
import com.coelhotechne.detection_system.sentinelRegistry.domain.enums.DroneEventType;

import java.time.Instant;
import java.util.UUID;

public record DroneFlightEventRecorded(
        UUID droneId,
        String droneName,
        UUID controllerNodeId,
        DroneEventType eventType,
        DroneEventSeverity severity,
        String message,
        Instant occurredAt
) {
}
