package com.coelhotechne.detection_system.autonomousDevice.api.dto;

import com.coelhotechne.detection_system.autonomousDevice.domain.AgentActionRecord;
import com.coelhotechne.detection_system.autonomousDevice.domain.enums.AgentDecisionOutcome;
import com.coelhotechne.detection_system.autonomousDevice.domain.enums.AutonomousCapability;
import com.coelhotechne.detection_system.security.domain.enums.Role;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.Instant;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AgentActionResponse(
        UUID uuid,
        UUID agentId,
        String requester,
        Role requesterRole,
        AutonomousCapability capability,
        String target,
        AgentDecisionOutcome outcome,
        String reason,
        Instant occurredAt
) {
    public static AgentActionResponse from(AgentActionRecord r){
        return new AgentActionResponse(
                r.getUuid(),
                r.getAgentId(),
                r.getRequester(),
                r.getRequesterRole(),
                r.getCapability(),
                r.getTarget(),
                r.getOutcome(),
                r.getReason(),
                r.getOccurredAt()
        );
    }
}
