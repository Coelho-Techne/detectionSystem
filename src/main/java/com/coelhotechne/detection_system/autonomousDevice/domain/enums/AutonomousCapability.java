package com.coelhotechne.detection_system.autonomousDevice.domain.enums;


public enum AutonomousCapability {
    DEVICE_READ(false),
    DEVICE_UPDATE(true),
    DEVICE_RESTART(true),
    DEVICE_DIAGNOSTICS(false),

    SENSOR_READ(false),
    SENSOR_CALIBRATION(true),

    CAMERA_READ(false),
    CAMERA_CONFIGURATION(true),

    ALERT_READ(false),
    ALERT_ACKNOWLEDGE(true),
    ALERT_RESOLVE(true),

    TELEMETRY_READ(false),

    SYSTEM_DIAGNOSTICS(false),
    SYSTEM_MAINTENANCE(true),

    REPORT_GENERATION(false);

    private final boolean mutating;

    AutonomousCapability(boolean mutating) {
        this.mutating = mutating;
    }

    public boolean isMutating() {
        return mutating;
    }
}
