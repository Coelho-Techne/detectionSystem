package com.coelhotechne.detection_system.detection.domain;

import com.coelhotechne.detection_system.detection.domain.enums.DetectionDeviceType;
import com.coelhotechne.detection_system.detection.domain.enums.DetectionType;
import com.coelhotechne.detection_system.sensor.domain.Sensor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "sensor_detection",
        indexes = @Index(name = "idx_sensor_detection_sensor", columnList = "sensor_id"))
public class SensorDetection extends Detection{
    public static final int UNIT_MAX = 16;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sensor_id", nullable = false, updatable = false)
    private Sensor sensor;

    /** Leitura que disparou a detecção (contrato v2, D12). Ex.: 850. */
    @Column(name = "measured_value", precision = 18, scale = 6, updatable = false)
    private BigDecimal measuredValue;

    /** Unidade da leitura. Ex.: {@code ppm}. */
    @Column(name = "measured_unit", length = UNIT_MAX, updatable = false)
    private String measuredUnit;

    protected SensorDetection(){
    }

    public SensorDetection(
            UUID sourceEventId,
            Sensor sensor,
            DetectionType detectionType,
            String unmappedType,
            Instant detectedAt,
            Instant receivedAt,
            BigDecimal confidence,
            BigDecimal measuredValue,
            String measuredUnit,
            String description
    ){
        super(sourceEventId,
                DetectionDeviceType.SENSOR,
                detectionType,
                unmappedType,
                Objects.requireNonNull(sensor,"Sensor is required").getZone(),
                detectedAt,
                receivedAt,
                confidence,
                description,
                sensor.hasApprovedHomologation());
        if (!DetectionTypeRules.allowsSensor(sensor.getSensorNiche(),detectionType)){
            throw new IllegalArgumentException(
                    "Detection type %s is not allowed for sensor niche %s"
                    .formatted(detectionType,sensor.getSensorNiche()));
        }
        this.sensor=sensor;
        this.measuredValue=measuredValue;
        this.measuredUnit=measuredValue == null ? null : truncate(measuredUnit,UNIT_MAX);
    }

    public Sensor getSensor(){
        return sensor;
    }

    public BigDecimal getMeasuredValue(){
        return measuredValue;
    }

    public String getMeasuredUnit(){return measuredUnit;}

    @Override
    public UUID getSourceId(){
        return sensor == null ? null : sensor.getUuid();
    }

}
