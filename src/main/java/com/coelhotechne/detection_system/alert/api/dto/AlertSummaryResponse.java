package com.coelhotechne.detection_system.alert.api.dto;

import com.coelhotechne.detection_system.alert.domain.enums.Severity;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AlertSummaryResponse(
        long totalOpen,
        Map<Severity, Long> bySeverity,
        List<ZoneTotal> byZone
) {
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record ZoneTotal(UUID zoneId, Severity severity, long total) {
    }
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record ZoneTotalName(UUID zoneId, String zoneName, long total) {
    }
}
