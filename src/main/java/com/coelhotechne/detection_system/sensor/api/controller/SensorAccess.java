package com.coelhotechne.detection_system.sensor.api.controller;

final class SensorAccess {
    static final String READ   = "hasAnyRole('ADMIN','TECHNICIAN','HOME_USER','AI_USER')"
            + " and @agentGuard.check('SENSOR_READ')";
    static final String WRITE = "hasAnyRole('ADMIN','TECHNICIAN','HOME_USER')";
    static final String DELETE = "hasRole('ADMIN')";
    static final String DECISION = "hasRole('ADMIN')";
    static final String TEST = "hasAnyRole('ADMIN','TECHNICIAN')";

    private SensorAccess() {}
}