package com.coelhotechne.detection_system.alert.api.dto;

import com.coelhotechne.detection_system.alert.domain.enums.*;
import com.coelhotechne.detection_system.detection.domain.enums.DetectionType;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.Instant;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AlertResponse(
        UUID uuid,
        Long version,
        AlertCategory category,
        AlertType type,
        Severity severity,
        AlertStatus status,
        AlertSourceType sourceType,
        UUID sourceId,
        UUID zoneId,
        String zoneName,
        String title,
        String description,
        int occurrenceCount,
        Instant firstOccurredAt,
        Instant lastOccurredAt,
        String acknowledgedBy,
        Instant acknowledgedAt,
        String resolvedBy,
        String resolutionNote,
        Instant closedAt,
        DetectionSummary detection
) {
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record DetectionSummary(UUID id, DetectionType type,Instant detectedAt){

    }
}
