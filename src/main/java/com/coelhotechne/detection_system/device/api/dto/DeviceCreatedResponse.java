package com.coelhotechne.detection_system.device.api.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record DeviceCreatedResponse(
        @JsonUnwrapped
        DeviceResponse deviceResponse,
        String accessKey
) {
}
