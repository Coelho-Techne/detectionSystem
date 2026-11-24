package com.coelhotechne.detection_system.sentinelRegistry.domain.enums;

public enum DroneEventType {
    TAKEOFF(DroneEventSeverity.INFO),
    LANDED(DroneEventSeverity.INFO),
    MISSION_STARTED(DroneEventSeverity.INFO),
    MISSION_COMPLETED(DroneEventSeverity.INFO),
    MISSION_ABORTED(DroneEventSeverity.WARNING),
    RETURN_TO_HOME(DroneEventSeverity.WARNING),
    WAYPOINT_REACHED(DroneEventSeverity.INFO),
    LOW_BATTERY(DroneEventSeverity.WARNING),
    CRITICAL_BATTERY(DroneEventSeverity.CRITICAL),
    LINK_LOST(DroneEventSeverity.CRITICAL),
    LINK_RESTORED(DroneEventSeverity.INFO),
    GPS_LOST(DroneEventSeverity.WARNING),
    GPS_RESTORED(DroneEventSeverity.INFO),
    GEOFENCE_BREACH(DroneEventSeverity.CRITICAL),
    OBSTACLE_DETECTED(DroneEventSeverity.INFO),
    EMERGENCY_LANDING(DroneEventSeverity.CRITICAL),
    MOTOR_FAILURE(DroneEventSeverity.CRITICAL),
    SENSOR_FAILURE(DroneEventSeverity.WARNING),
    CAMERA_STARTED(DroneEventSeverity.INFO),
    CAMERA_STOPPED(DroneEventSeverity.INFO),
    COMMAND_SENT(DroneEventSeverity.INFO),
    COMMAND_ACKNOWLEDGED(DroneEventSeverity.INFO),
    COMMAND_REJECTED(DroneEventSeverity.WARNING),
    FIRMWARE_UPDATED(DroneEventSeverity.INFO),
    MAINTENANCE_REQUIRED(DroneEventSeverity.WARNING),
    ERROR(DroneEventSeverity.WARNING),
    OTHER(DroneEventSeverity.INFO);

    private final DroneEventSeverity defaultSeverity;

    DroneEventType(DroneEventSeverity defaultSeverity){
        this.defaultSeverity = defaultSeverity;
    }

    public DroneEventSeverity defaultSeverity(){
        return defaultSeverity;
    }

    public boolean isCommandAudit(){
        return this == COMMAND_SENT || this == COMMAND_ACKNOWLEDGED || this == COMMAND_REJECTED;
    }
}
