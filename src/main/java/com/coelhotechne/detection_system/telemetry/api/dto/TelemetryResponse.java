package com.coelhotechne.detection_system.telemetry.api.dto;

import com.coelhotechne.detection_system.telemetry.domain.Telemetry;
import com.coelhotechne.detection_system.telemetry.domain.enums.TelemetryMetric;
import com.coelhotechne.detection_system.telemetry.domain.enums.TelemetrySource;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonPropertyOrder({
        "uuid",
        "sensorId",
        "zoneId",
        "metric",
        "value",
        "unit",
        "measuredAt",
        "receivedAt",
        "source",
        "createdBy"
})
public record TelemetryResponse(
        UUID uuid,
        UUID sensorId,
        UUID zoneId,
        TelemetryMetric metric,
        BigDecimal value,
        String unit,
        Instant measuredAt,
        Instant receivedAt,
        TelemetrySource source,
        String createdBy
) {
}