package com.coelhotechne.detection_system.sentinelRegistry.api.dto;

final class DroneConstraints {

    static final String SERIAL_PATTERN = "^[A-Za-z0-9._-]+$";
    static final String PROTOCOL_PATTERN = "^[A-Za-z0-9._ /-]*$";

    private DroneConstraints(){}
}
