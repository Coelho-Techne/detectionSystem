package com.coelhotechne.detection_system.autonomousDevice.exceptions;

import com.coelhotechne.detection_system.autonomousDevice.domain.AgentDecision;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.UUID;

@Getter
public class AgentForbiddenException extends ErrorResponseException {
    private final UUID agentId;
    private final transient AgentDecision decision;
    public AgentForbiddenException(UUID agentId,AgentDecision decision) {
        super(HttpStatus.FORBIDDEN,buildProblemDetail(agentId,decision),null);
        this.agentId = agentId;
        this.decision = decision;
    }
    private static ProblemDetail buildProblemDetail(UUID agentId,AgentDecision decision){
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN,decision.reason());
        pd.setTitle("Agent Action Not Allowed");
        pd.setProperty("agentId",agentId);
        pd.setProperty("outcome",decision.outcome());
        pd.setProperty("capability",decision.capability() == null ? null : decision.capability().name());
        pd.setProperty("requesterRole",decision.requesterRole() == null ? null : decision.requesterRole().name());
        return pd;
    }
}
