package com.coelhotechne.detection_system.sentinelRegistry.api.controller;

final class DroneAccess {
    static final String READ = "hasAnyRole('ADMIN','TECHNICIAN','HOME_USER','AI_USER')";
    static final String HANDLE = "hasAnyRole('ADMIN','TECHNICIAN','HOME_USER')";

    static final String READ_AGENT = READ + " and @agentGuard.check('ALERT_READ')";
    static final String ACKNOWLEDGE = HANDLE + " and @agentGuard.check('ALERT_ACKNOWLEDGE')";
    static final String RESOLVE = HANDLE + " and @agentGuard.check('ALERT_RESOLVE')";

    private DroneAccess(){}
}
