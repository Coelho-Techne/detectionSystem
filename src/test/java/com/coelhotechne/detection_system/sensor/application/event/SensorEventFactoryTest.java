package com.coelhotechne.detection_system.sensor.application.event;

import com.coelhotechne.detection_system.sensor.domain.payload.SensorDetectionPayload;
import com.coelhotechne.detection_system.sensor.event.SensorDetectionEvent;
import com.coelhotechne.detection_system.sensor.mqtt.SensorTopic;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class SensorEventFactoryTest {

    private static final Instant NOW  = Instant.parse("2026-10-09T23:41:05Z");
    private static final UUID SENSOR  = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private final SensorEventFactory factory =
            new SensorEventFactory(JsonMapper.builder().build(),
                    Clock.fixed(NOW, ZoneOffset.UTC));
    @Test
    void v2RedeliveryKeepsTheSameEventId(){
        String json = """
                 {"v":2,"event_id":"5c1d8f0a-2b7e-4a39-8f0e-2d6a1b9c4e77","category":"gas_leak",
                 "detected_at":"2026-10-09T23:41:00Z","value":850,"unit":"ppm"}
                """;
        SensorDetectionEvent first = (SensorDetectionEvent) factory.create(new SensorTopic(SENSOR,"detection"),json);
        SensorDetectionEvent again = (SensorDetectionEvent) factory.create(new SensorTopic(SENSOR,"detection"),json);

        assertThat(again.eventId()).isEqualTo(first.eventId());
        assertThat(first.detectedAt()).isEqualTo(Instant.parse("2026-10-09T23:41:00Z"));
        assertThat(first.occurredAt()).isEqualTo(NOW);
    }
    @Test
    void sameDeviceEventIdOnTwoSensorsDoesNotCollide(){
        SensorDetectionPayload payload = new SensorDetectionPayload(
                2,
                UUID.fromString("5c1d8f0a-2b7e-4a39-8f0e-2d6a1b9c4e77"),
                "smoke",
                null,
                null,
                null,
                null,
                null
        );
        UUID other = UUID.fromString("22222222-2222-2222-2222-222222222222");
        assertThat(SensorEventFactory.detectionEventId(SENSOR,payload))
                .isNotEqualTo(SensorEventFactory.detectionEventId(other,payload));
    }
    @Test
    void v1WithDetectedAtIsDeterministic(){
        SensorDetectionPayload v1 = new SensorDetectionPayload(
                null,
                null,
                "door_open",
                null,
                Instant.parse("2026-10-09T23:40:00Z"),
                null,
                null,
                null);

        assertThat(SensorEventFactory.detectionEventId(SENSOR,v1))
                .isEqualTo(SensorEventFactory.detectionEventId(SENSOR,v1));
    }
    @Test
    void v1WithoutDetectedAtFallsBackToServerTime(){
        String json = """
                 {"category":"door_open","description":"porta da frente"}
                """;
        SensorDetectionEvent event = (SensorDetectionEvent) factory.create(new SensorTopic(SENSOR,"detection"),null);
        assertThat(event.detectedAt()).isEqualTo(NOW);
    }
}
