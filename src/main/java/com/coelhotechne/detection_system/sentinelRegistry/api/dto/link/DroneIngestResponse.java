package com.coelhotechne.detection_system.sentinelRegistry.api.dto.link;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.Instant;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record DroneIngestResponse(
        UUID droneId,
        int accepted,
        int duplicates,
        Instant receivedAt
) {
}
