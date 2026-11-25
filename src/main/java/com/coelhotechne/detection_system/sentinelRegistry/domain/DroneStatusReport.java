package com.coelhotechne.detection_system.sentinelRegistry.domain;

import com.coelhotechne.detection_system.sentinelRegistry.domain.enums.DroneFlightState;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "drone_status_report",
        indexes = @Index(name = "idx_drone_status_report_time", columnList = "drone_uuid, recorded_at"),
        uniqueConstraints = @UniqueConstraint(name = "uk_drone_status_report", columnNames = {"drone_uuid", "report_id"}))
public class DroneStatusReport {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

    @Column(name = "drone_uuid", nullable = false, updatable = false)
    private UUID droneId;

    @Column(name = "controller_node_uuid", nullable = false, updatable = false)
    private UUID controllerNodeId;

    @Column(name = "report_id", nullable = false, updatable = false)
    private UUID reportId;

    /** Hora da leitura no drone/controladora. */
    @Column(name = "recorded_at", nullable = false, updatable = false)
    private Instant recordedAt;

    /** Hora em que chegou aqui. */
    @Column(name = "received_at", nullable = false, updatable = false)
    private Instant receivedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "flight_state", nullable = false, length = 20, updatable = false)
    private DroneFlightState flightState;

    @Column(name = "latitude", updatable = false)
    private Double latitude;
    @Column(name = "longitude", updatable = false)
    private Double longitude;
    /** Altura em relação ao ponto de decolagem. */
    @Column(name = "altitude_meters", updatable = false)
    private Double altitudeMeters;
    @Column(name = "heading_degrees", updatable = false)
    private Double headingDegrees;
    @Column(name = "ground_speed_ms", updatable = false)
    private Double groundSpeedMs;
    @Column(name = "battery_percent", updatable = false)
    private Double batteryPercent;
    @Column(name = "battery_voltage", updatable = false)
    private Double batteryVoltage;
    @Column(name = "link_quality_percent", updatable = false)
    private Integer linkQualityPercent;
    @Column(name = "rssi_dbm", updatable = false)
    private Integer rssiDbm;
    @Column(name = "satellites", updatable = false)
    private Integer satellites;
    @Column(name = "mission_id", length = 64, updatable = false)
    private String missionId;

    public DroneStatusReport(UUID droneId, UUID controllerNodeId, UUID reportId, Instant recordedAt, Instant receivedAt,
                             DroneFlightState flightState, Double latitude, Double longitude, Double altitudeMeters,
                             Double headingDegrees, Double groundSpeedMs, Double batteryPercent, Double batteryVoltage,
                             Integer linkQualityPercent, Integer rssiDbm, Integer satellites, String missionId) {
        this.droneId = Objects.requireNonNull(droneId, "droneId is required");
        this.controllerNodeId = Objects.requireNonNull(controllerNodeId, "controllerNodeId is required");
        this.reportId = Objects.requireNonNull(reportId, "reportId is required");
        this.recordedAt = Objects.requireNonNull(recordedAt, "recordedAt is required");
        this.receivedAt = Objects.requireNonNull(receivedAt, "receivedAt is required");
        this.flightState = flightState != null ? flightState : DroneFlightState.UNKNOWN;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitudeMeters = altitudeMeters;
        this.headingDegrees = headingDegrees;
        this.groundSpeedMs = groundSpeedMs;
        this.batteryPercent = batteryPercent;
        this.batteryVoltage = batteryVoltage;
        this.linkQualityPercent = linkQualityPercent;
        this.rssiDbm = rssiDbm;
        this.satellites = satellites;
        this.missionId = missionId;
    }
}
