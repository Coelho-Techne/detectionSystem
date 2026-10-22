package com.coelhotechne.detection_system.alert.api.dto;

import com.coelhotechne.detection_system.alert.domain.Alert;
import com.coelhotechne.detection_system.alert.domain.enums.Severity;
import com.coelhotechne.detection_system.alert.infrastructure.AlertRepository;
import com.coelhotechne.detection_system.detection.domain.Detection;
import com.coelhotechne.detection_system.zone.domain.Zone;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class AlertMapper {

    public AlertResponse toResponse(Alert alert){
        Objects.requireNonNull(alert,"Alert cannot be null");
        Zone zone = alert.getZone();
        Detection detection = alert.getDetection();
        return new AlertResponse(
                alert.getUuid(),
                alert.getVersion(),
                alert.getCategory(),
                alert.getType(),
                alert.getSeverity(),
                alert.getStatus(),
                alert.getSourceType(),
                alert.getSourceId(),
                zone == null ? null : zone.getUuid(),
                zone == null ? null : zone.getName(),
                alert.getTitle(),
                alert.getDescription(),
                alert.getOccurrenceCount(),
                alert.getFirstOccurredAt(),
                alert.getLastOccurredAt(),
                alert.getAcknowledgedBy(),
                alert.getAcknowledgedAt(),
                alert.getResolvedBy(),
                alert.getResolutionNote(),
                alert.getClosedAt(),
                detection == null ? null : new AlertResponse.DetectionSummary(
                        detection.getUuid(), detection.getType(), detection.getDetectedAt()));
    }

    public AlertSummaryResponse toSummary(List<AlertRepository.SeverityCount> bySeverity,
                                          List<AlertRepository.ZoneCount> byZone) {
        Map<Severity, Long> severities = new EnumMap<>(Severity.class);
        for (Severity s : Severity.values()) {
            severities.put(s, 0L);
        }
        long total = 0;
        for (AlertRepository.SeverityCount c : bySeverity) {
            severities.put(c.getSeverity(), c.getTotal());
            total += c.getTotal();
        }
        List<AlertSummaryResponse.ZoneTotal> zones = byZone.stream()
                .map(z -> new AlertSummaryResponse.ZoneTotal(z.getZoneId(),
                        z.getZoneName(), z.getTotal()))
                .toList();
        return new AlertSummaryResponse(total, severities, zones);
    }
}
