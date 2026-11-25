package com.coelhotechne.detection_system.sentinelRegistry.api.dto;

import com.coelhotechne.detection_system.sentinelRegistry.domain.enums.DroneControlType;
import com.coelhotechne.detection_system.sentinelRegistry.domain.enums.DronePurpose;
import com.coelhotechne.detection_system.sentinelRegistry.domain.enums.DroneType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface DroneData {
    String droneName();
    String manufacturer();
    String model();
    DroneType droneType();
    DronePurpose dronePurpose();
    DroneControlType controlType();
    Integer weightGrams();
    Integer maxPayloadGrams();
    Integer maxFlightTimeMinutes();
    Double maxSpeedKmh();
    Double maxAltitudeMeters();
    Double maxRangeMeters();
    String firmwareVersion();
    String communicationProtocol();
    LocalDate manufacturedAt();
    LocalDateTime lastMaintenanceAt();
    String ipAddress();
    Boolean status();
}
