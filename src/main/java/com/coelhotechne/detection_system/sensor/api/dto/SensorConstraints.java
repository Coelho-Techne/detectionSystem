package com.coelhotechne.detection_system.sensor.api.dto;

public final class SensorConstraints {
    public static final String NAME_PATTERN = "^[A-Za-z0-9_-]{1,15}$";

    public static final String NAME_MESSAGE =
            "The name must have up to 15 alphanumeric characters, '-' or '_' (it is an MQTT topic segment)";

    private SensorConstraints() {
    }
}
