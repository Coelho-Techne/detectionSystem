package com.coelhotechne.detection_system.sensor.domain.homologation;

import com.coelhotechne.detection_system.sensor.domain.homologation.enums.SensorSelfTestFailure;

public record SensorSelfTestOutcome(boolean success,
                                    SensorSelfTestFailure failure,
                                    String detail) {

    public SensorSelfTestOutcome {
        if (success && failure != null) {
            throw new IllegalArgumentException("Successful outcome cannot carry a failure type");
        }
        if (!success && failure == null) {
            throw new IllegalArgumentException("Failed outcome requires a failure type");
        }
    }

    public static SensorSelfTestOutcome passed(String detail) {
        return new SensorSelfTestOutcome(true, null, detail);
    }

    public static SensorSelfTestOutcome failed(SensorSelfTestFailure failure, String detail) {
        return new SensorSelfTestOutcome(false, failure, detail);
    }
}