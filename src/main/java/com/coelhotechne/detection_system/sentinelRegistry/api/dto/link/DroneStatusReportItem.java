package com.coelhotechne.detection_system.sentinelRegistry.api.dto.link;

import com.coelhotechne.detection_system.sentinelRegistry.domain.enums.DroneFlightState;
import jakarta.validation.constraints.*;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.Instant;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record DroneStatusReportItem(
        @NotNull
        UUID reportId,
        @NotNull
        Instant recordedAt,
        @NotNull
        DroneFlightState flightState,
        @DecimalMin("-90.0") @DecimalMax("90.0")
        Double latitude,
        @DecimalMin("-180.0") @DecimalMax("180.0")
        Double longitude,
        @DecimalMin("-500.0") @DecimalMax("10000.0")
        Double altitudeMeters,
        @DecimalMin("0.0") @DecimalMax(value = "360.0", inclusive = false)
        Double headingDegrees,
        @DecimalMin("0.0") @DecimalMax("200.0")
        Double groundSpeedMs,
        @DecimalMin("0.0") @DecimalMax("100.0")
        Double batteryPercent,
        @DecimalMin("0.0") @DecimalMax("100.0")
        Double batteryVoltage,
        @Min(0)@Max(100)
        Integer linkQualityPercent,
        @Min(-150)@Max(0)
        Integer rssiDbm,
        @Min(0)@Max(100)
        Integer satellites,
        @Size(max = 64)
        String missionId
) {
}
