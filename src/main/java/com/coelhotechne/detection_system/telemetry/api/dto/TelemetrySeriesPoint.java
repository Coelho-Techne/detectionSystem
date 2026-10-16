package com.coelhotechne.detection_system.telemetry.api.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;
import java.time.Instant;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record TelemetrySeriesPoint (
        Instant bucketStart,
        BigDecimal avg,
        BigDecimal min,
        BigDecimal max,
        long samples
){
}
