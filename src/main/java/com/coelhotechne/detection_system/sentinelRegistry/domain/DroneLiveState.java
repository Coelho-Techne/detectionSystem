package com.coelhotechne.detection_system.sentinelRegistry.domain;

import com.coelhotechne.detection_system.sentinelRegistry.domain.enums.DroneEventType;
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
@Table(name = "drone_live_state")
public class DroneLiveState {

    @Id
    @Column(name = "drone_id")
    private UUID droneId;
    @Column(name = "controller_node_uuid")
    private UUID controllerNodeId;
    @Enumerated(EnumType.STRING)
    @Column(name = "flight_state",nullable = false,length = 20)
    private DroneFlightState flightState;
    @Column(name = "latitude")
    private Double latitude;
    @Column(name = "longitude")
    private Double longitude;
    @Column(name = "altitude_meters")
    private Double altitudeMeters;
    @Column(name = "heading_degrees")
    private Double headingDegrees;
    @Column(name = "ground_speed_ms")
    private Double groundSpeedMs;
    @Column(name = "battery_percent")
    private Double batteryPercent;
    @Column(name = "link_quality_percent")
    private Integer linkQualityPercent;
    @Column(name = "satellites")
    private Integer satellites;
    @Column(name = "mission_id",length = 64)
    private String missionId;
    @Column(name = "last_recorded_at")
    private Instant lastRecordedAt;
    @Column(name = "last_received_at")
    private Instant lastReceivedAt;
    @Enumerated(EnumType.STRING)
    @Column(name = "last_event_type",length = 30)
    private DroneEventType lastEventType;
    @Column(name = "last_event_at")
    private Instant lastEventAt;



    public DroneLiveState(UUID droneId,UUID controllerNodeId){
        this.droneId = Objects.requireNonNull(droneId,"droneId is required");
        this.controllerNodeId = controllerNodeId;
        this.flightState = DroneFlightState.UNKNOWN;
    }

    public void assignController(UUID controllerNodeId) {
        this.controllerNodeId = controllerNodeId;
    }

    public boolean apply(DroneStatusReport r){
        if (lastRecordedAt!=null && r.getRecordedAt().isBefore(lastRecordedAt)){
            return false;
        }
        this.controllerNodeId = r.getControllerNodeId();
        this.flightState = r.getFlightState();
        this.latitude = r.getLatitude();
        this.longitude = r.getLongitude();
        this.altitudeMeters = r.getAltitudeMeters();
        this.headingDegrees = r.getHeadingDegrees();
        this.groundSpeedMs = r.getGroundSpeedMs();
        this.batteryPercent = r.getBatteryPercent();
        this.linkQualityPercent = r.getLinkQualityPercent();
        this.satellites = r.getSatellites();
        this.missionId = r.getMissionId();
        this.lastRecordedAt = r.getRecordedAt();
        this.lastReceivedAt = r.getReceivedAt();
        return true;
    }

    public void apply(DroneFlightEvent e){
        if (lastEventAt!=null&& e.getOccurredAt().isBefore(lastEventAt)){
            return;
        }
        this.lastEventType = e.getEventType();
        this.lastEventAt = e.getOccurredAt();
        this.lastReceivedAt = e.getReceivedAt();
        if (e.getEventType() == DroneEventType.LINK_LOST){
            this.flightState  = DroneFlightState.LINK_LOST;
        } else if (e.getEventType() == DroneEventType.EMERGENCY_LANDING) {
            this.flightState = DroneFlightState.EMERGENCY;
        }
    }
}
