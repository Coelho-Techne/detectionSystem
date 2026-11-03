package com.coelhotechne.detection_system.zone.api.dto;

import com.coelhotechne.detection_system.zone.domain.enums.ZoneType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record ZoneRequest(
        @NotBlank
        @Pattern(regexp = "^[^/+#]{1,100}$", message = "name must have up to 100 characters and no '/', '+' or '#'")
        String name,
        String description,
        @NotNull
        ZoneType zoneType,
        @NotNull
        BigDecimal centerLatitude,
        @NotNull
        BigDecimal centerLongitude,
        @NotNull
        @Positive
        Double radiusMeters,
        @NotNull
        Boolean active
) {
}