package com.coelhotechne.detection_system.globalClass.security;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.Instant;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record IssuedAccessKeyResponse(
        UUID deviceId,
        String accessKey,
        Instant issuedAt
) {
}
