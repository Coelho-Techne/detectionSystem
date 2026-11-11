package com.coelhotechne.detection_system.sensor.domain.payload;

import com.coelhotechne.detection_system.sensor.application.event.SensorEventFactory;
import com.coelhotechne.detection_system.sensor.domain.homologation.SensorSelfTestOutcome;
import com.coelhotechne.detection_system.sensor.domain.homologation.enums.SensorSelfTestFailure;
import com.coelhotechne.detection_system.sensor.event.SensorSelfTestResultEvent;
import com.coelhotechne.detection_system.sensor.exceptions.SensorEventParseException;
import com.coelhotechne.detection_system.sensor.mqtt.SensorTopic;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SensorSelfTestPayloadTest {
    private static final UUID SENSOR = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private final SensorEventFactory factory = new SensorEventFactory(JsonMapper.builder().build()
    ,Clock.fixed(Instant.parse("2026-11-08T11:36:00Z"),ZoneOffset.UTC));

    @Test
    void passedResponseBecomesEvent(){
        UUID req = UUID.randomUUID();
        SensorSelfTestResultEvent event = (SensorSelfTestResultEvent) factory.create(new SensorTopic(SENSOR,"selftest")
        ,"""
                {"request_id":"%s","passed":true,"firmware_version":"1.4.2","checks":{"sensor_read":true}}
                """.formatted(req));
        assertThat(event.requestId()).isEqualTo(req);
        assertThat(event.outcome().success()).isTrue();
        assertThat(event.outcome().detail()).contains("1.4.2");
    }
    @Test
    void failedWithoutErrorListsFailedChecks(){
        SensorSelfTestOutcome outcome = new SensorSelfTestPayload(UUID.randomUUID(),
                false,
                "1.0",
                null,
                Map.of("memory",false,"sensor_read",true)).toOutcome();
        assertThat(outcome.failure()).isEqualTo(SensorSelfTestFailure.DEVICE_REPORTED);
        assertThat(outcome.detail()).contains("memory").doesNotContain("sensor_read");
    }
    @Test
    void missingPassedIsInvalidResponse(){
        SensorSelfTestOutcome outcome = new SensorSelfTestPayload(UUID.randomUUID(),
                null,
                null,
                null,
                null).toOutcome();
        assertThat(outcome.failure()).isEqualTo(SensorSelfTestFailure.INVALID_RESPONSE);
    }
    @Test
    void missingRequestIdIsRejected(){
        assertThatThrownBy(()-> factory.create(new SensorTopic(SENSOR,"selftest"),
                "{\"passed\":true}")).isInstanceOf(SensorEventParseException.class);
    }
}
