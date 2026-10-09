package com.coelhotechne.detection_system.detection.api.dto;

import com.coelhotechne.detection_system.detection.domain.enums.DetectionDeviceType;
import com.coelhotechne.detection_system.detection.domain.enums.DetectionType;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonPropertyOrder({
        "uuid",
        "sourceEventId",
        "deviceType",
        "sourceId",
        "type",
        "unmappedType",
        "zoneId",
        "zoneName",
        "detectedAt",
        "receivedAt",
        "confidence",
        "description"
})
public record DetectionResponse(
        UUID uuid,
        UUID sourceEventId,
        DetectionDeviceType deviceType,
        UUID sourceId,
        DetectionType type,
        String unmappedType,
        UUID zoneId,
        String zoneName,
        Instant detectedAt,
        Instant receivedAt,
        BigDecimal confidence,
        String description
) {
}
