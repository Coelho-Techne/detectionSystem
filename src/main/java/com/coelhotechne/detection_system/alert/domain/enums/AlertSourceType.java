package com.coelhotechne.detection_system.alert.domain.enums;

import com.coelhotechne.detection_system.detection.domain.enums.DetectionDeviceType;

public enum AlertSourceType {
    SENSOR,
    CAMERA,
    DEVICE,
    SYSTEM;

    public static AlertSourceType of(DetectionDeviceType deviceType){
        if (deviceType == null){
            return SYSTEM;
        }

        return switch (deviceType){
            case SENSOR -> SENSOR;
            case CAMERA -> CAMERA;
            default -> DEVICE;
        };
    }
}
