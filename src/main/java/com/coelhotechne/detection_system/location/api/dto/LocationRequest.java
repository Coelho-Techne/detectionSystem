package com.coelhotechne.detection_system.location.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record LocationRequest(
        @NotBlank
        String name,
        @NotNull
        BigDecimal latitude,
        @NotNull
        BigDecimal longitude,
        @NotNull
        UUID zoneUuid
) {
}