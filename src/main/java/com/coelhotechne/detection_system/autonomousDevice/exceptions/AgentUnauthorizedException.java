package com.coelhotechne.detection_system.autonomousDevice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.UUID;

public class AgentUnauthorizedException extends ErrorResponseException {
    private final String agentName;
    private final UUID agentId;
    public AgentUnauthorizedException(String agentName,UUID agentId, String cause) {
        super(HttpStatus.UNAUTHORIZED,buildProblemDetail(agentName,agentId,cause),null);
        this.agentId=agentId;
        this.agentName=agentName;
    }

    private static ProblemDetail buildProblemDetail(String agentName,UUID agentId,String cause){
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,cause);
        pd.setTitle("Agent Unauthorized");
        pd.setProperty("agentName",agentName);
        pd.setProperty("agentId",agentId);
        return pd;
    }
}
