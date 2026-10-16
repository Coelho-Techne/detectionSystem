package com.coelhotechne.detection_system.telemetry.application;

import com.coelhotechne.detection_system.telemetry.api.dto.TelemetryRequest;
import com.coelhotechne.detection_system.telemetry.api.dto.TelemetryResponse;
import com.coelhotechne.detection_system.telemetry.api.dto.TelemetrySeriesPoint;
import com.coelhotechne.detection_system.telemetry.domain.Telemetry;
import com.coelhotechne.detection_system.telemetry.domain.enums.TelemetryMetric;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TelemetryService {
    Page<TelemetryResponse>search(TelemetryFilter filter,Pageable pageable);
    TelemetryResponse get(UUID id);

    List<TelemetryResponse>latest(UUID sensorId);
    List<TelemetrySeriesPoint>series(UUID sensorId, TelemetryMetric metric, Instant from,
                                     Instant to, Duration bucket);

    TelemetryResponse createManual(TelemetryRequest request);

    void delete(UUID uuid);
}
