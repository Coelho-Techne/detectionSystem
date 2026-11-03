package com.coelhotechne.detection_system.security.api.controller;

final class UserAccess {
    static final String READ   = "hasAnyRole('ADMIN','TECHNICIAN','HOME_USER','AI_USER')";
    static final String WRITE_A_T_H = "hasAnyRole('ADMIN','TECHNICIAN','HOME_USER')";
    static final String ONLY_ADM = "hasRole('ADMIN')";
    static final String EDIT  = "hasAnyRole('ADMIN','TECHNICIAN')";
    static final String AUTH = "isAuthenticated() and !hasAnyAuthority('DEVICE_CAM','DEVICE_SENSOR')";
    private UserAccess(){}
}
