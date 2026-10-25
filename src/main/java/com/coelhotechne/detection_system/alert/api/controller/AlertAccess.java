package com.coelhotechne.detection_system.alert.api.controller;

final class AlertAccess {

    static final String READ = "hasAnyRole('ADMIN','TECHNICIAN','HOME_USER','AI_USER')";
    /** AI_USER só lê (Role.AI_USER: "ainda não autorizada a realizar alterações"). */
    static final String HANDLE = "hasAnyRole('ADMIN','TECHNICIAN','HOME_USER')";

    private AlertAccess() {}
}
