package com.coelhotechne.detection_system.sensor.event;

import java.time.Instant;
import java.util.UUID;

public sealed interface SensorEvent permits
        SensorDetectionEvent,
        SensorStatusReportedEvent,
        SensorStatusEvent,
        SensorTelemetryEvent,
        SensorPowerSupplyEvent,
        SensorSelfTestResultEvent
{
    UUID eventId();
    UUID sensorId();
    Instant occurredAt();
}