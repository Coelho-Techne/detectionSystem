package com.coelhotechne.detection_system.telemetry.application;

import com.coelhotechne.detection_system.telemetry.domain.enums.TelemetryMetric;
import com.coelhotechne.detection_system.telemetry.exceptions.TelemetryInvalidQueryException;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Slf4j
public record TelemetryFilter(
        UUID sensorId,
        UUID zoneId,
        TelemetryMetric metric,
        Instant from,
        Instant to
) {

    public static final Duration DEFAULT_WINDOW = Duration.ofHours(24);
    public static final Duration MAX_WINDOW = Duration.ofDays(31);

    public TelemetryFilter withDefaults(Instant now){
        Instant end = to != null ? to : now;
        Instant start = from != null ? from : end.minus(DEFAULT_WINDOW);

        if (!start.isBefore(end)){
            throw new TelemetryInvalidQueryException("'from' must be before 'to'");
        }
        if (Duration.between(start,end).compareTo(MAX_WINDOW)>0){
            throw new TelemetryInvalidQueryException("period must be at most "+MAX_WINDOW.toDays() + " days");
        }
        return new TelemetryFilter(
                sensorId,
                zoneId,
                metric,
                start,
                end);
    }
}
