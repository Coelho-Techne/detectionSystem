package com.coelhotechne.detection_system.sentinelRegistry.api.dto.link;

import com.coelhotechne.detection_system.sentinelRegistry.domain.Drone;
import com.coelhotechne.detection_system.sentinelRegistry.domain.DroneLiveState;
import com.coelhotechne.detection_system.sentinelRegistry.domain.enums.DroneEventType;
import com.coelhotechne.detection_system.sentinelRegistry.domain.enums.DroneFlightState;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.Instant;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record DroneLiveStateResponse(
        UUID droneId,
        String droneName,
        boolean active,
        UUID controllerNodeId,
        Instant coupledAt,
        DroneFlightState flightState,
        Double latitude,
        Double longitude,
        Double altitudeMeters,
        Double headingDegrees,
        Double groundSpeedMs,
        Double batteryPercent,
        Integer linkQualityPercent,
        Integer satellites,
        String missionId,
        Instant lastRecordedAt,
        Instant lastReceivedAt,
        DroneEventType lastEventType,
        Instant lastEventAt
) {
    public static DroneLiveStateResponse of(Drone d, DroneLiveState s){
        if (s == null){
            return new DroneLiveStateResponse(
                    d.getUuid(),
                    d.getDroneName(),
                    d.isActive(),
                    d.getControllerNodeId(),
                    d.getCoupledAt(),
                    DroneFlightState.UNKNOWN,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
            );
        }
        return new DroneLiveStateResponse(
                d.getUuid(),
                d.getDroneName(),
                d.isActive(),
                d.getControllerNodeId(),
                d.getCoupledAt(),
                s.getFlightState(),
                s.getLatitude(),
                s.getLongitude(),
                s.getAltitudeMeters(),
                s.getHeadingDegrees(),
                s.getGroundSpeedMs(),
                s.getBatteryPercent(),
                s.getLinkQualityPercent(),
                s.getSatellites(),
                s.getMissionId(),
                s.getLastRecordedAt(),
                s.getLastReceivedAt(),
                s.getLastEventType(),
                s.getLastEventAt()
        );
    }
}
