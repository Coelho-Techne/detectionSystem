package com.coelhotechne.detection_system.detection.domain;

import com.coelhotechne.detection_system.detection.domain.enums.DetectionDeviceType;
import com.coelhotechne.detection_system.detection.domain.enums.DetectionType;
import com.coelhotechne.detection_system.globalClass.entities.BaseEntity;
import com.coelhotechne.detection_system.zone.domain.Zone;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "detection",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_detection_source_event", columnNames = "source_event_id"),
        indexes = {
                @Index(name = "idx_detection_detected_at", columnList = "detected_at"),
                @Index(name = "idx_detection_zone_detected_at", columnList = "zone_id, detected_at"),
                @Index(name = "idx_detection_type_detected_at", columnList = "detection_type, detected_at")
        })
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Detection extends BaseEntity {

    public static final int DESCRIPTION_MAX = 255;
    public static final int UNMAPPED_TYPE_MAX = 50;

    @Column(name = "source_event_id", nullable = false, updatable = false)
    private UUID sourceEventId;
    @Enumerated(EnumType.STRING)
    @Column(name = "detection_device_type", nullable = false, updatable = false, length = 20)
    private DetectionDeviceType deviceType;
    @Enumerated(EnumType.STRING)
    @Column(name = "detection_type", nullable = false, updatable = false, length = 40)
    private DetectionType type;
    @Column(name = "unmapped_type", updatable = false, length = UNMAPPED_TYPE_MAX)
    private String unmappedType;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id", updatable = false)
    private Zone zone;
    @Column(name = "detected_at", nullable = false, updatable = false)
    private Instant detectedAt;
    @Column(name = "received_at", nullable = false, updatable = false)
    private Instant receivedAt;
    /** 0.000–1.000. Nulo para sensor binário. */
    @Column(name = "confidence", precision = 4, scale = 3, updatable = false)
    private BigDecimal confidence;

    /** Texto livre opcional enviado pela origem. */
    @Column(name = "description", updatable = false, length = DESCRIPTION_MAX)
    private String description;

    /** Exclusivo do JPA. */
    protected Detection() {
    }

    protected Detection(UUID sourceEventId,
                        DetectionDeviceType deviceType,
                        DetectionType type,
                        String unmappedType,
                        Zone zone,
                        Instant detectedAt,
                        Instant receivedAt,
                        BigDecimal confidence,
                        String description) {
        this.sourceEventId = Objects.requireNonNull(sourceEventId, "sourceEventId is required");
        this.deviceType = Objects.requireNonNull(deviceType, "source is required");
        this.type = Objects.requireNonNull(type, "type is required");
        this.unmappedType = type == DetectionType.UNKNOWN ? truncate(unmappedType, UNMAPPED_TYPE_MAX) : null;
        this.zone = zone;
        this.detectedAt = Objects.requireNonNull(detectedAt, "detectedAt is required");
        this.receivedAt = Objects.requireNonNull(receivedAt, "receivedAt is required");
        this.confidence = requireValidConfidence(confidence);
        this.description = truncate(description, DESCRIPTION_MAX);
    }

    /** UUID do dispositivo que detectou (sensor, câmera...). */
    public abstract UUID getSourceId();

    public UUID getSourceEventId() {
        return sourceEventId;
    }

    public DetectionDeviceType getDeviceType() {
        return deviceType;
    }

    public DetectionType getType() {
        return type;
    }

    public String getUnmappedType() {
        return unmappedType;
    }

    public Zone getZone() {
        return zone;
    }

    public Instant getDetectedAt() {
        return detectedAt;
    }

    public Instant getReceivedAt() {
        return receivedAt;
    }

    public BigDecimal getConfidence() {
        return confidence;
    }

    public String getDescription() {
        return description;
    }
    /** Normaliza o valor da confianca de uma detecao por porcentagem */
    private static BigDecimal requireValidConfidence(BigDecimal confidence) {
        if (confidence == null) {
            return null;
        }
        if (confidence.signum() < 0 || confidence.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("confidence must be between 0 and 1: " + confidence);
        }
        return confidence.setScale(3, RoundingMode.HALF_UP);
    }
    /** Retira espacos antes e depois da frase e limita pelo max */
    private static String truncate(String value, int max) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() <= max ? trimmed : trimmed.substring(0, max);
    }
}
