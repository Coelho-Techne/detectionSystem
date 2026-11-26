package com.coelhotechne.detection_system.sentinelRegistry.api.dto.link;

import com.coelhotechne.detection_system.sentinelRegistry.domain.DroneStatusReport;
import com.coelhotechne.detection_system.sentinelRegistry.domain.enums.DroneFlightState;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.Instant;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record DroneStatusReportResponse (
        UUID reportId,
        UUID controllerNodeId,
        Instant recordedAt,
        Instant receivedAt,
        DroneFlightState flightState,
        Double latitude,
        Double longitude,
        Double altitudeMeters,
        Double headingDegrees,
        Double groundSpeedMs,
        Double batteryPercent,
        Double batteryVoltage,
        Integer linkQualityPercent,
        Integer rssiDbm,
        Integer satellites,
        String missionId
){
    public static DroneStatusReportResponse of(DroneStatusReport r){
        return new DroneStatusReportResponse(
                r.getReportId(),
                r.getControllerNodeId(),
                r.getRecordedAt(),
                r.getReceivedAt(),
                r.getFlightState(),
                r.getLatitude(),
                r.getLongitude(),
                r.getAltitudeMeters(),
                r.getHeadingDegrees(),
                r.getGroundSpeedMs(),
                r.getBatteryPercent(),
                r.getBatteryVoltage(),
                r.getLinkQualityPercent(),
                r.getRssiDbm(),
                r.getSatellites(),
                r.getMissionId()
        );
    }
}
