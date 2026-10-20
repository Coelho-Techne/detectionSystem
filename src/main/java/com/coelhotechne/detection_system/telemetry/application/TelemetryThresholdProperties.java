package com.coelhotechne.detection_system.telemetry.application;

import com.coelhotechne.detection_system.telemetry.domain.enums.TelemetryMetric;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.util.Map;

@ConfigurationProperties(prefix = "telemetry.threshold")
public record TelemetryThresholdProperties(
        Map<TelemetryMetric, Limit>limits
){

    public TelemetryThresholdProperties {
        limits = limits == null ? Map.of() : Map.copyOf(limits);
    }

    public record  Limit(
            BigDecimal min,
            BigDecimal max,
            BigDecimal hysteresis
    ){
        public Limit{
            hysteresis = hysteresis == null ? BigDecimal.ZERO : hysteresis.abs();
        }
    }
}
