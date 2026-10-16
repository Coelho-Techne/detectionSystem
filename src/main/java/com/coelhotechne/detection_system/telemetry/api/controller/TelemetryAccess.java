package com.coelhotechne.detection_system.telemetry.api.controller;

final class TelemetryAccess {
    static final String READ = "hasAnyRole('ADMIN','TECHNICIAN','HOME_USER','AI_USER')";
    static final String WRITE = "hasAnyRole('ADMIN','TECHNICIAN')";
    static final String DELETE = "hasRole('ADMIN')";
    private TelemetryAccess(){}
}
