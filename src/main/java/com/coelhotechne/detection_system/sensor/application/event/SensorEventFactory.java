package com.coelhotechne.detection_system.sensor.application.event;

import com.coelhotechne.detection_system.globalClass.time.DeviceClock;
import com.coelhotechne.detection_system.sensor.domain.payload.SensorTelemetryPayload;
import com.coelhotechne.detection_system.sensor.domain.payload.SensorDetectionPayload;
import com.coelhotechne.detection_system.sensor.domain.payload.SensorStatusPayload;
import com.coelhotechne.detection_system.sensor.event.*;
import com.coelhotechne.detection_system.sensor.exceptions.SensorEventParseException;
import com.coelhotechne.detection_system.sensor.exceptions.UnsupportedSensorEventException;
import com.coelhotechne.detection_system.sensor.mqtt.SensorTopic;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SensorEventFactory {

    private final JsonMapper objectMapper;
    private final Clock clock;

    public SensorEvent create(SensorTopic topic, String payload) {
        try {
            return switch (topic.eventType()) {
                case "detection" -> createDetection(topic.sensorId(), payload);
                case "status" -> createStatus(topic.sensorId(), payload);
                case "telemetry" -> createTelemetry(topic.sensorId(), payload);
                default -> throw new UnsupportedSensorEventException(topic.eventType());
            };
        } catch (JacksonException e) {
            throw new SensorEventParseException(
                    "Invalid sensor event payload for type '%s'".formatted(topic.eventType()), e);
        }
    }

    private SensorDetectionEvent createDetection(UUID sensorId, String payload) {
        SensorDetectionPayload detection = objectMapper.readValue(payload, SensorDetectionPayload.class);
        Instant recivedAt = clock.instant();
        return new SensorDetectionEvent(
                detectionEventId(sensorId,detection),
                sensorId,
                detection,
                recivedAt,
                DeviceClock.resolve(detection.detectedAt(),recivedAt)
        );
    }

    private SensorStatusReportedEvent createStatus(UUID sensorId, String payload) {
        SensorStatusPayload status = objectMapper.readValue(payload, SensorStatusPayload.class);
        return new SensorStatusReportedEvent(UUID.randomUUID(), sensorId, status.toSensorStatus(), clock.instant());
    }

    private SensorTelemetryEvent createTelemetry(UUID sensorId, String payload) {
        SensorTelemetryPayload telemetry = objectMapper.readValue(payload, SensorTelemetryPayload.class);
        Instant receivedAt = clock.instant();
        UUID eventId = telemetry.msgId() != null ? nameUuid(sensorId+ "|" + telemetry.msgId()):UUID.randomUUID();
        return new SensorTelemetryEvent(UUID.randomUUID(), sensorId, telemetry,DeviceClock.resolve(telemetry.measuredAt(),receivedAt),receivedAt );
    }

    static UUID detectionEventId(UUID sensorId,SensorDetectionPayload payload){
        if (payload.eventId()!=null){
            return nameUuid(sensorId+"|"+ payload.eventId());
        }
        if (payload.detectedAt()!=null){
            return nameUuid(sensorId + "|" + payload.category() + "|" + payload.detectedAt());
        }
        return UUID.randomUUID();
    }

    private static UUID nameUuid(String key) {
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
    }
}