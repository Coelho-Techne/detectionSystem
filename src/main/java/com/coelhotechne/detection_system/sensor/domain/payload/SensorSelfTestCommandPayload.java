package com.coelhotechne.detection_system.sensor.domain.payload;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.Instant;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SensorSelfTestCommandPayload (
        String command,
        UUID requestId, // o firmware precisa devolver este valor no ack
        Instant inssuedAt
){
}
