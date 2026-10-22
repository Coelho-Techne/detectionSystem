package com.coelhotechne.detection_system.alert.application;

import com.coelhotechne.detection_system.alert.domain.enums.AlertCategory;
import com.coelhotechne.detection_system.alert.domain.enums.AlertStatus;
import com.coelhotechne.detection_system.alert.domain.enums.Severity;

import java.time.Instant;
import java.util.UUID;

public record AlertFilter(
        AlertStatus status,
        Severity severity,
        AlertCategory category,
        UUID zoneId,
        UUID sourceId,
        Instant from,
        Instant to
) {
}
