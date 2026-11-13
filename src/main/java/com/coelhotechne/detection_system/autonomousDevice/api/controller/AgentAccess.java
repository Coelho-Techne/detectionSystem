package com.coelhotechne.detection_system.autonomousDevice.api.controller;

final class AgentAccess {
    static final String READ = "hasAnyRole('ADMIN','TECHNICIAN','HOME_USER','AI_USER')";
    static final String AUDIT = "hasAnyRole('ADMIN','TECHNICIAN')";
    static final String ADMIN = "hasRole('ADMIN')";
    static final String REQUESTER = "isAuthenticated() and !hasAnyAuthority('DEVICE_CAM','DEVICE_SENSOR')";
    private AgentAccess(){}
}
