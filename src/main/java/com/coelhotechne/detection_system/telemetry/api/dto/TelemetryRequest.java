package com.coelhotechne.detection_system.telemetry.api.dto;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record TelemetryRequest(
        @NotNull
        UUID sensorId,
        @NotBlank @Size(max = 40)
        String metric,
        @NotNull
        BigDecimal value,
        Instant measuredAt
) {
}