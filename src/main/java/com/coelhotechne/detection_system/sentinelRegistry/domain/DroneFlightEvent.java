package com.coelhotechne.detection_system.sentinelRegistry.domain;

import com.coelhotechne.detection_system.sentinelRegistry.domain.enums.DroneEventSeverity;
import com.coelhotechne.detection_system.sentinelRegistry.domain.enums.DroneEventType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "drone_flight_event",
        indexes = {
                @Index(name = "idx_drone_flight_event_time", columnList = "drone_uuid, occurred_at"),
                @Index(name = "idx_drone_flight_event_type", columnList = "event_type, occurred_at")
        },
        uniqueConstraints = @UniqueConstraint(name = "uk_drone_flight_event", columnNames = {"drone_uuid", "event_id"}))
public class DroneFlightEvent {

    public static final int MESSAGE_MAX = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID uuid;

    @Column(name = "drone_uuid", nullable = false, updatable = false)
    private UUID droneId;

    @Column(name = "controller_node_uuid", nullable = false, updatable = false)
    private UUID controllerNodeId;

    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 30, updatable = false)
    private DroneEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(name = "severity", nullable = false, length = 10, updatable = false)
    private DroneEventSeverity severity;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    @Column(name = "received_at", nullable = false, updatable = false)
    private Instant receivedAt;

    @Column(name = "mission_id", length = 64, updatable = false)
    private String missionId;

    @Column(name = "command", length = 40, updatable = false)
    private String command;

    @Column(name = "message", length = MESSAGE_MAX, updatable = false)
    private String message;

    @Column(name = "latitude", updatable = false)
    private Double latitude;

    @Column(name = "longitude", updatable = false)
    private Double longitude;

    public DroneFlightEvent(UUID droneId,
                            UUID controllerNodeId,
                            UUID eventId,
                            DroneEventType eventType,
                            DroneEventSeverity severity,
                            Instant occurredAt,
                            Instant receivedAt,
                            String missionId,
                            String command,
                            String message,
                            Double latitude,
                            Double longitude){
        this.droneId = Objects.requireNonNull(droneId, "droneId is required");
        this.controllerNodeId = Objects.requireNonNull(controllerNodeId, "controllerNodeId is required");
        this.eventId = Objects.requireNonNull(eventId, "eventId is required");
        this.eventType = Objects.requireNonNull(eventType, "eventType is required");
        this.severity = severity != null ? severity : eventType.defaultSeverity();
        this.occurredAt = Objects.requireNonNull(occurredAt, "occurredAt is required");
        this.receivedAt = Objects.requireNonNull(receivedAt, "receivedAt is required");
        this.missionId = missionId;
        this.command = command;
        this.message = message == null || message.length() <= MESSAGE_MAX ? message : message.substring(0, MESSAGE_MAX);
        this.latitude = latitude;
        this.longitude = longitude;
    }

}
