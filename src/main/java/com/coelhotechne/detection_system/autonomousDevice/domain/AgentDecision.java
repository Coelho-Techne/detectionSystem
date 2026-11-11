package com.coelhotechne.detection_system.autonomousDevice.domain;

import com.coelhotechne.detection_system.autonomousDevice.domain.enums.AutonomousCapability;
import com.coelhotechne.detection_system.security.domain.enums.Role;

import java.util.Objects;

public record AgentDecision(
        AgentDecisionOutcome outcome,
        AutonomousCapability capability,
        Role requesterRole,
        String reason
) {
    public AgentDecision{
        Objects.requireNonNull(outcome,"outcome is required");
        Objects.requireNonNull(reason,"reason is required");
    }

    public static AgentDecision allowed(AutonomousCapability capability,Role role){
        return new AgentDecision(AgentDecisionOutcome.ALLOWED,capability,role,"allowed");
    }
    public static AgentDecision approval(AutonomousCapability capability,Role role,String reason){
        return new AgentDecision(AgentDecisionOutcome.REQUIRES_APPROVAL,capability,role,reason);
    } public static AgentDecision denied(AutonomousCapability capability,Role role,String reason){
        return new AgentDecision(AgentDecisionOutcome.DENIED,capability,role,reason);
    } public boolean isAllowed(){
        return outcome == AgentDecisionOutcome.ALLOWED;
    }
}
