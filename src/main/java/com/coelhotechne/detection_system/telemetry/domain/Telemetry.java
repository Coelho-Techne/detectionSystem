package com.coelhotechne.detection_system.telemetry.domain;

import com.coelhotechne.detection_system.globalClass.entities.BaseEntity;
import com.coelhotechne.detection_system.sensor.domain.Sensor;
import com.coelhotechne.detection_system.telemetry.domain.enums.TelemetryMetric;
import com.coelhotechne.detection_system.telemetry.domain.enums.TelemetrySource;
import com.coelhotechne.detection_system.zone.domain.Zone;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;


@Entity
@Table(name = "telemetry",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_telemetry_source_message",
                columnNames = {"sensor_id", "source_message_id", "metric"}),
        indexes = {
                @Index(name = "idx_telemetry_sensor_metric_measured", columnList = "sensor_id, metric, measured_at"),
                @Index(name = "idx_telemetry_zone_measured", columnList = "zone_id, measured_at")
        })
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Telemetry extends BaseEntity {

    public static final int VALUE_SCALE = 6;

    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    @JoinColumn(name = "sensor_id", nullable = false,updatable = false)
    private Sensor sensor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", updatable = false)
    private Zone zone;

    @Enumerated(EnumType.STRING)
    @Column(name = "metric",nullable = false,updatable = false,length = 40)
    private TelemetryMetric metric;

    @Column(name = "value",nullable = false,updatable = false,precision = 18,scale = VALUE_SCALE)
    private BigDecimal value;

    @Column(name = "unit",nullable = false,updatable = false,length = 16)
    private String unit;

    @Column(name = "measured_at",nullable = false,updatable = false)
    private Instant measuredAt;

    @Column(name = "received_at", nullable = false,updatable = false)
    private Instant receivedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_message_id",updatable = false)
    private TelemetrySource source;

    @Column(name = "source_message_id",updatable = false)
    private UUID sourceMessageId;

    protected Telemetry(){

    }

    public Telemetry(
            Sensor sensor,
            TelemetryMetric metric,
            BigDecimal value,
            Instant measuredAt,
            Instant receivedAt,
            TelemetrySource source,
            UUID sourceMessageId
    ){
        this.sensor = Objects.requireNonNull(sensor,"sensor is required");
        this.metric = Objects.requireNonNull(metric,"metric is required");
        if (!metric.allowsNiche(sensor.getSensorNiche())){
            throw new IllegalArgumentException("Metric %s is not allowed for sensor niche %s"
                    .formatted(metric,sensor.getSensorNiche()));
        }
        this.value = Objects.requireNonNull(value,"value is required")
                .setScale(VALUE_SCALE, RoundingMode.HALF_DOWN);
        this.zone = sensor.getZone();
        this.measuredAt = Objects.requireNonNull(measuredAt,"measuredAt is required");
        this.receivedAt = Objects.requireNonNull(receivedAt,"receivedAt is required");
        this.source = Objects.requireNonNull(source,"source is required");
        this.sourceMessageId = sourceMessageId;
    }

    public Sensor getSensor(){
        return sensor;
    }
    public Zone getZone(){
        return zone;
    }

    public TelemetryMetric getMetric(){
        return metric;
    }

    public BigDecimal getValue(){
        return value;
    }

    public String getUnit(){
        return unit;
    }

    public Instant getMeasuredAt(){
        return measuredAt;
    }

    public Instant getReceivedAt(){
        return receivedAt;
    }

    public TelemetrySource getSource(){
        return source;
    }
    public UUID getSourceMessageId(){
        return sourceMessageId;
    }
}
