package com.coelhotechne.detection_system.telemetry.api.dto;

import com.coelhotechne.detection_system.telemetry.domain.Telemetry;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class TelemetryMapper  {

    public TelemetryResponse toResponse(Telemetry entity) {
        Objects.requireNonNull(entity,"Telemetry cannot be null");
        return new TelemetryResponse(
                entity.getUuid(),
                entity.getSensor()== null ? null : entity.getSensor().getUuid(),
                entity.getZone() == null ? null : entity.getZone().getUuid(),
                entity.getMetric(),
                entity.getValue(),
                entity.getUnit(),
                entity.getMeasuredAt(),
                entity.getMeasuredAt(),
                entity.getSource(),
                entity.getCreatedBy()
        );
    }
}
