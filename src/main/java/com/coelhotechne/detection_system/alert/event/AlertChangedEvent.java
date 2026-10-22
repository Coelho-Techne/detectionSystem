package com.coelhotechne.detection_system.alert.event;

import com.coelhotechne.detection_system.alert.domain.enums.AlertStatus;
import com.coelhotechne.detection_system.alert.domain.enums.Severity;

import java.util.UUID;

public record AlertChangedEvent(
        UUID alertId,
        Change change,
        AlertStatus status,
        Severity severity,
        UUID zoneId
) {
    public enum Change{
        CREATED,
        REPEATED,
        ACKNOWLEDGE,
        RESOLVED,
        CLEARED
    }
}
