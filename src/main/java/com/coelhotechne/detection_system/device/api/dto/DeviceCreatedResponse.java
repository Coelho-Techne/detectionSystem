package com.coelhotechne.detection_system.device.api.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record DeviceCreatedResponse(
        DeviceResponse deviceResponse,
        String accessKey
) {
}
