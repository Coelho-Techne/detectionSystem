package com.coelhotechne.detection_system.autonomousDevice.api.dto;

import com.coelhotechne.detection_system.autonomousDevice.domain.enums.AutonomousCapability;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AgentDecisionRequest(
        @NotNull
        AutonomousCapability capability,
        @Size(max = 120)
        String target
) {
}
