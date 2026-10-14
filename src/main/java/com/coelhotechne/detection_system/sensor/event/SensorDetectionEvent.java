package com.coelhotechne.detection_system.sensor.event;

import com.coelhotechne.detection_system.sensor.domain.payload.SensorDetectionPayload;

import java.time.Instant;
import java.util.UUID;

public record SensorDetectionEvent(
        UUID eventId,
        UUID sensorId,
        SensorDetectionPayload detection,
        Instant occurredAt,
        Instant detectedAt
) implements SensorEvent {
}

