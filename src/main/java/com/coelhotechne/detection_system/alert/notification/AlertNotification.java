package com.coelhotechne.detection_system.alert.notification;

import com.coelhotechne.detection_system.alert.domain.enums.AlertCategory;
import com.coelhotechne.detection_system.alert.domain.enums.AlertStatus;
import com.coelhotechne.detection_system.alert.domain.enums.Severity;
import com.coelhotechne.detection_system.alert.event.AlertChangedEvent;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.Instant;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AlertNotification(
        UUID alertId,
        AlertChangedEvent.Change changedEvent,
        AlertStatus status,
        Severity severity,
        AlertCategory category,
        String title,
        UUID zoneId,
        String zoneName,
        int occurrenceCount,
        Instant lastOccurredAt
) {
}
