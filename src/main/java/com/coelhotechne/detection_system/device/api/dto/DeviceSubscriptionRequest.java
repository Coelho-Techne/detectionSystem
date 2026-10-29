package com.coelhotechne.detection_system.device.api.dto;

import com.coelhotechne.detection_system.alert.domain.enums.Severity;
import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record DeviceSubscriptionRequest(
        UUID zoneId,
        @NotNull
        Severity minSeverity
) {
}
