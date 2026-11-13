package com.coelhotechne.detection_system.autonomousDevice.api.dto;

import com.coelhotechne.detection_system.autonomousDevice.domain.AgentDecision;
import com.coelhotechne.detection_system.autonomousDevice.domain.enums.AgentDecisionOutcome;
import com.coelhotechne.detection_system.autonomousDevice.domain.enums.AutonomousCapability;
import com.coelhotechne.detection_system.security.domain.enums.Role;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AgentDecisionResponse(
        UUID agentId,
        AgentDecisionOutcome outcome,
        AutonomousCapability capability,
        Role requesterRole,
        String reason
) {
    public static AgentDecisionResponse of(UUID agentId,AgentDecision d){
        return new AgentDecisionResponse(
                agentId,
                d.outcome(),
                d.capability(),
                d.requesterRole(),
                d.reason()
        );
    }
}
