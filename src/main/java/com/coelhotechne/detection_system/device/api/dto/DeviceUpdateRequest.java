package com.coelhotechne.detection_system.device.api.dto;

import jakarta.validation.constraints.NotBlank;

public record DeviceUpdateRequest(
        @NotBlank
        String currentAccessKey, // usado para autenticar, não para persistir
        @NotBlank
        String deviceName,
        @NotBlank
        String deviceType,
        @NotBlank
        String ipAddress,
        Boolean status
) {
}
