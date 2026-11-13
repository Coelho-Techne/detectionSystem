package com.coelhotechne.detection_system.device.api.controller;

final class DeviceAccess {
    static final String READ   = "hasAnyRole('ADMIN','TECHNICIAN','HOME_USER','AI_USER')";
    static final String WRITE_A_T_H = "hasAnyRole('ADMIN','TECHNICIAN','HOME_USER')";
    static final String ONLY_ADM = "hasRole('ADMIN')";
    static final String EDIT    = "hasAnyRole('ADMIN','TECHNICIAN')";
    static final String READ_AGENT = READ + " and @agentGuard.check('DEVICE_READ')";
    static final String EDIT_AGENT = EDIT + " and @agentGuard.check('DEVICE_UPDATE')";
    private DeviceAccess(){}
}
