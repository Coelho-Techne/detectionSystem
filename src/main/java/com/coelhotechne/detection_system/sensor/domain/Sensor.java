package com.coelhotechne.detection_system.sensor.domain;

import com.coelhotechne.detection_system.batterysupply.domain.PowerSupply;
import com.coelhotechne.detection_system.globalClass.entities.BaseEntity;
import com.coelhotechne.detection_system.installation.domain.Installation;
import com.coelhotechne.detection_system.sensor.domain.enums.SensorNiche;
import com.coelhotechne.detection_system.sensor.domain.homologation.SensorHomologationRecord;
import com.coelhotechne.detection_system.sensor.domain.homologation.enums.SensorHomologationStatus;
import com.coelhotechne.detection_system.sensor.domain.payload.SensorTelemetryPayload;
import com.coelhotechne.detection_system.sensor.domain.enums.SensorStatus;
import com.coelhotechne.detection_system.zone.domain.Zone;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.OptimisticLock;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;


import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.regex.Pattern;


@Setter
@Getter
@Entity
@DynamicUpdate
@NoArgsConstructor
@Table(name = "sensor",
        uniqueConstraints = @UniqueConstraint(
        name = "uk_sensor_name_zone", columnNames = {"name", "zone_id"}),
        indexes = {
                @Index(name = "idx_sensor_zone", columnList = "zone_id"),
                @Index(name = "idx_sensor_status", columnList = "sensor_status"),
                @Index(name = "idx_sensor_homologation",
                        columnList = "homologation_status, homologation_test_requested_at")
        }
)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@EntityListeners(AuditingEntityListener.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Sensor extends BaseEntity {
    private static final Pattern TOPIC_SAFE_NAME = Pattern.compile("^[A-Za-z0-9_-]{1,15}$");
    private static final int DATA_DESCRIPTION_MAX = 255;

    @Column(nullable = false,name = "name",length = 15)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(name = "sensor_niche",nullable = false)
    private SensorNiche sensorNiche;
    @Enumerated(EnumType.STRING)
    @Column(name = "sensor_status", nullable = false, length = 30)
    private SensorStatus sensorStatus = SensorStatus.INITIALIZING;
    @Column(name = "activation_time", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", shape = JsonFormat.Shape.STRING)
    private LocalDateTime activationTime;
    @OptimisticLock(excluded = true)
    @Column(name = "memory_used", precision = 15, scale = 2)
    private BigDecimal memoryUsed;
    @OptimisticLock(excluded = true)
    @Column(name = "data_transfer_value", precision = 15, scale = 2)
    private BigDecimal dataTransferValue;
    @Column(name = "data_description", nullable = false, length = DATA_DESCRIPTION_MAX)
    private String dataDescription;
    @OptimisticLock(excluded = true)
    @Column(name = "last_communication")
    private Instant lastCommunication;
    @OptimisticLock(excluded = true)
    @Column(name = "last_reading_at")
    private Instant lastReadingAt;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "status",      column = @Column(name = "installation_status", length = 20)),
            @AttributeOverride(name = "installedAt", column = @Column(name = "installation_installed_at"))
    })
    @EqualsAndHashCode.Exclude
    private Installation installation;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "zone_id", nullable = false)
    @EqualsAndHashCode.Exclude
    private Zone zone;
    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "status",        column = @Column(name = "power_supply_status", length = 20)),
            @AttributeOverride(name = "type",          column = @Column(name = "power_supply_type", length = 20)),
            @AttributeOverride(name = "percentage",    column = @Column(name = "power_supply_percentage", precision = 5, scale = 2)),
            @AttributeOverride(name = "lastReadingAt", column = @Column(name = "power_supply_last_reading_at"))
    })
    @EqualsAndHashCode.Exclude
    private PowerSupply powerSupply;
    @Embedded
    @JsonIgnore
    @Setter(AccessLevel.NONE)
    @EqualsAndHashCode.Exclude
    private SensorHomologationRecord homologation = new SensorHomologationRecord();
    @JsonIgnore
    @Column(name = "access_key", unique = true, length = 64)
    private String accessKey;

    //:::::::::::::::::::::::: GETTERS ::::::::::::::::::::::::
    public SensorHomologationRecord getHomologation(){
        if (homologation==null){
            homologation=new SensorHomologationRecord();
        }
        return homologation;
    }

    public boolean hasApprovedHomologation() {
        return getHomologation().getStatus() == SensorHomologationStatus.APPROVED;
    }
    @PrePersist
    @PreUpdate
    private void validateInvariants(){
    if (name==null||!TOPIC_SAFE_NAME.matcher(name).matches()){
        throw new IllegalArgumentException("The sensor name is invalid for use with the MQTT topic:'"+name+"'");
    }
    }

    public record DiagnosticsOutcome(
            SensorStatus previousStatus,
            SensorStatus currentStatus,
            boolean persist
    ) {
        public boolean statusChanged() {
            return previousStatus != currentStatus;
        }

        static DiagnosticsOutcome unchanged(SensorStatus current) {
            return new DiagnosticsOutcome(current, current, false);
        }
    }

    public DiagnosticsOutcome applyDiagnostics(SensorTelemetryPayload payload,
                                               Instant observedAt,
                                               SensorDiagnosticsThresholds thresholds) {
        SensorStatus previous = this.sensorStatus;

        if (isOutOfOrder(observedAt)) {
            return DiagnosticsOutcome.unchanged(previous);
        }

        SensorStatus resolved = resolveStatus(payload, thresholds);
        boolean statusChanged = resolved != previous;

        if (!statusChanged && !isHeartbeatDue(observedAt, thresholds.heartbeatWriteInterval())) {
            return DiagnosticsOutcome.unchanged(previous);
        }

        if (payload.memoryUsed() != null) {
            this.memoryUsed = payload.memoryUsed();
        }
        if (payload.dataTransferValue() != null) {
            this.dataTransferValue = payload.dataTransferValue();
        }
        if (payload.dataDescription() != null && !payload.dataDescription().isBlank()) {
            this.dataDescription = truncate(payload.dataDescription());
        }
        this.sensorStatus = resolved;
        this.lastReadingAt = observedAt;

        return new DiagnosticsOutcome(previous, resolved, true);
    }

    private SensorStatus resolveStatus(SensorTelemetryPayload payload,
                                       SensorDiagnosticsThresholds thresholds) {
        if (Boolean.FALSE.equals(payload.status())) {
            return SensorStatus.FAULT;
        }
        if (this.sensorStatus == SensorStatus.MAINTENANCE_REQUIRED) {
            return SensorStatus.MAINTENANCE_REQUIRED;
        }
        if (payload.dataTransferValue() != null
                && payload.dataTransferValue().compareTo(thresholds.dataTransferOutOfSpec()) > 0) {
            return SensorStatus.OUT_OF_SPECIFICATION;
        }
        return SensorStatus.OK;
    }

    public DiagnosticsOutcome applyReportedStatus(SensorStatus reported,
                                                  Instant observedAt,
                                                  SensorDiagnosticsThresholds thresholds) {
        SensorStatus previous = this.sensorStatus;

        if (reported == null || !reported.isDeviceReportable() || isOutOfOrder(observedAt)) {
            return DiagnosticsOutcome.unchanged(previous);
        }

        boolean maintenanceHold = previous == SensorStatus.MAINTENANCE_REQUIRED
                && reported != SensorStatus.FAULT;
        boolean statusChanged = !maintenanceHold && previous != reported;

        if (!statusChanged && !isHeartbeatDue(observedAt, thresholds.heartbeatWriteInterval())) {
            return DiagnosticsOutcome.unchanged(previous);
        }

        this.lastReadingAt = observedAt;
        if (statusChanged) {
            this.sensorStatus = reported;
        }
        return new DiagnosticsOutcome(previous, this.sensorStatus, true);
    }

    public boolean requestMaintenance() {
        if (this.sensorStatus == SensorStatus.MAINTENANCE_REQUIRED) {
            return false;
        }
        this.sensorStatus = SensorStatus.MAINTENANCE_REQUIRED;
        return true;
    }


    private boolean isOutOfOrder(Instant observedAt) {
        return lastReadingAt != null && observedAt.isBefore(lastReadingAt);
    }

    private boolean isHeartbeatDue(Instant observedAt, Duration heartbeatWriteInterval) {
        return lastReadingAt == null
                || Duration.between(lastReadingAt, observedAt).compareTo(heartbeatWriteInterval) >= 0;
    }

    public boolean clearMaintenance() {
        if (this.sensorStatus != SensorStatus.MAINTENANCE_REQUIRED) {
            return false;
        }
        this.sensorStatus= SensorStatus.INITIALIZING;
        return true;
    }

    private static String truncate(String value) {
        return value.length() <= DATA_DESCRIPTION_MAX
                ? value
                : value.substring(0, DATA_DESCRIPTION_MAX);
    }

    public boolean markDisconnected() {
        if (this.sensorStatus == SensorStatus.DISCONNECTED) {
            return false;
        }
        this.sensorStatus = SensorStatus.DISCONNECTED;
        return true;
    }

    public void touchCommunication(Instant at) {
        this.lastCommunication = at;
    }
}
