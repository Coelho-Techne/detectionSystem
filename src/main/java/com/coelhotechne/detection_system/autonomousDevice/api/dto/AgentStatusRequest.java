package com.coelhotechne.detection_system.autonomousDevice.api.dto;

import com.coelhotechne.detection_system.autonomousDevice.domain.enums.AutonomousAgentStatus;
import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AgentStatusRequest(
        @NotNull
        AutonomousAgentStatus status
) {
}
