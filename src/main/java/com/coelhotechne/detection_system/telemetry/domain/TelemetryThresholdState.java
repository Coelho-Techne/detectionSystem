package com.coelhotechne.detection_system.telemetry.domain;


import com.coelhotechne.detection_system.telemetry.domain.enums.TelemetryMetric;
import com.coelhotechne.detection_system.telemetry.event.TelemetryThresholdEvent.Band;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "telemetry_threshold_state")
public class TelemetryThresholdState {

    @Id
    @Column(name = "state_key",length = 80)
    private String key;
    @Column(name = "sensor_id",nullable = false)
    private UUID sensorId;
    @Enumerated(EnumType.STRING)
    @Column(name = "metric",nullable = false,length = 40)
    private TelemetryMetric metric;
    @Enumerated(EnumType.STRING)
    @Column(name = "band",nullable = false,length = 10)
    private Band band;
    @Column(name = "updated_at",nullable = false)
    private Instant updatedAt;

    public static String key(UUID sensorId,TelemetryMetric metric){
        return sensorId +":"+metric.name();
    }
}

