package com.coelhotechne.detection_system.detection.domain;

import com.coelhotechne.detection_system.detection.domain.enums.DetectionDeviceType;
import com.coelhotechne.detection_system.detection.domain.enums.DetectionType;
import com.coelhotechne.detection_system.sensor.domain.Sensor;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "sensor_detection",
        indexes = @Index(name = "idx_sensor_detection_sensor", columnList = "sensor_id"))
public class SensorDetection extends Detection{

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sensor_id", nullable = false, updatable = false)
    private Sensor sensor;

    protected SensorDetection(){
    }

    public SensorDetection(
            UUID sourceEventId,
            Sensor sensor,
            DetectionType detectionType,
            String unmappedType,
            Instant detectedAt,
            Instant recivedAt,
            String description
    ){
        super(sourceEventId,
                DetectionDeviceType.SENSOR,
                detectionType,
                unmappedType,
                Objects.requireNonNull(sensor,"Sensor is required").getZone(),
                detectedAt,
                recivedAt,
                null,
                description);
        if (!DetectionTypeRules.allowsSensor(sensor.getSensorNiche(),detectionType)){
            throw new IllegalArgumentException(
                    "Detection type %s is not allowed for sensor niche %s"
                    .formatted(detectionType,sensor.getSensorNiche()));
        }
        this.sensor=sensor;
    }

    public Sensor getSensor(){
        return sensor;
    }

    @Override
    public UUID getSourceId(){
        return sensor == null ? null : sensor.getUuid();
    }

}
