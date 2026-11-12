package com.coelhotechne.detection_system.autonomousDevice.api.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AgentCreatedResponse(
        @JsonUnwrapped
        AgentResponse agentResponse,
        String accessKey
) {
}
