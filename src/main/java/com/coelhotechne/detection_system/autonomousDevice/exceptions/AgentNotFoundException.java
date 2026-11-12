package com.coelhotechne.detection_system.autonomousDevice.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.UUID;
@Getter
public class AgentNotFoundException extends ErrorResponseException {
    private final UUID agentId;
    public AgentNotFoundException(UUID agentId) {
        super(HttpStatus.NOT_FOUND,buildProblemDetail(agentId),null);
        this.agentId = agentId;
    }

    private static ProblemDetail buildProblemDetail(UUID agentId){
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,"Agent not found");
        pd.setTitle("Agent Not Found");
        pd.setProperty("agentId",agentId);
        return pd;
    }
}
