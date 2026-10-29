package com.coelhotechne.detection_system.device.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;


@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record DeviceRequest(
        @NotBlank
        @Size(max = 255)
        String deviceName,
        @NotBlank
        @Size(max = 255)
        String deviceType,
        @NotBlank
        @Size(max = 64)
        String ipAddress,
        Boolean status
) {
}
