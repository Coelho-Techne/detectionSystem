package com.coelhotechne.detection_system.detection.api.controller;

final class DetectionAccess {
    static final String READ   = "hasAnyRole('ADMIN','TECHNICIAN','HOME_USER','AI_USER')";
    static final String WRITE_A_T_H = "hasAnyRole('ADMIN','TECHNICIAN','HOME_USER')";
    static final String ONLY_ADM = "hasRole('ADMIN')";
    static final String EDIT    = "hasAnyRole('ADMIN','TECHNICIAN')";
    static final String EDIT_AUTH  = "hasAuthority('DEVICE_CAM') or hasRole('ADMIN')";
    private DetectionAccess() {}
}
