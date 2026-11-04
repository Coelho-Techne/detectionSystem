package com.coelhotechne.detection_system.sensor.event;

import com.coelhotechne.detection_system.sensor.domain.homologation.SensorSelfTestOutcome;

import java.time.Instant;
import java.util.UUID;

public record SensorSelfTestResultEvent (
        UUID eventId,
        UUID sensorId,
        UUID requestId,
        SensorSelfTestOutcome outcome,
        Instant occurredAt
)implements SensorEvent {
}
