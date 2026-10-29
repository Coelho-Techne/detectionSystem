package com.coelhotechne.detection_system.device.api.dto;

import jakarta.validation.constraints.NotBlank;

public record DeviceRegistrationRequest(
        @NotBlank
        String deviceName,
        @NotBlank
        String deviceType,
        @NotBlank
        String ipAddress
) {
}
