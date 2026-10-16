package com.coelhotechne.detection_system.telemetry.event;

import com.coelhotechne.detection_system.telemetry.domain.enums.TelemetryMetric;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TelemetryThresholdEvent(
        UUID eventId,
        UUID sensorId,
        UUID zoneId,
        TelemetryMetric metric,
        BigDecimal value,
        BigDecimal limit,
        Band previusBand,
        Band band,
        Instant measuredAt
) {
    public enum Band{
        BELOW,
        NORMAL,
        ABOVE
    }
}
