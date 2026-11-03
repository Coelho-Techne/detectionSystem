package com.coelhotechne.detection_system.location.api.controller;

public class LocationAccess {
    static final String READ   = "hasAnyRole('ADMIN','TECHNICIAN','HOME_USER','AI_USER')";
    static final String WRITE_A_T_H = "hasAnyRole('ADMIN','TECHNICIAN','HOME_USER')";
    static final String ONLY_ADM = "hasRole('ADMIN')";
    static final String EDIT    = "hasAnyRole('ADMIN','TECHNICIAN')";
    private LocationAccess(){}
}
