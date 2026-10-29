package com.coelhotechne.detection_system.device.api.dto;

import com.coelhotechne.detection_system.alert.domain.enums.Severity;
import com.coelhotechne.detection_system.device.domain.DeviceAlertSubscription;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record DeviceSubscriptionResponse(
        UUID uuid,
        UUID deviceId,
        UUID zoneId,
        String zoneName,
        Severity minSeverity,
        boolean active
) {
    public static DeviceSubscriptionResponse from(DeviceAlertSubscription s){
        return new DeviceSubscriptionResponse(
                s.getUuid(),
                s.getDevice().getUuid(),
                s.getZone() == null ? null : s.getZone().getUuid(),
                s.getZone() == null ? null : s.getZone().getName(),
                s.getMinSeverity(),
                s.isActive());
    }
}
