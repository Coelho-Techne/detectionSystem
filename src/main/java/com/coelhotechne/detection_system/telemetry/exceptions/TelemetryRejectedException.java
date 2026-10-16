package com.coelhotechne.detection_system.telemetry.exceptions;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class TelemetryRejectedException extends RuntimeException {
    public TelemetryRejectedException(@NotNull UUID uuid, String message) {
        super(message);
    }
}
