package com.coelhotechne.detection_system.device.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.UUID;

public class DeviceIsAgentException extends ErrorResponseException {
    public DeviceIsAgentException(UUID deviceId) {
        super(HttpStatus.CONFLICT,buildProblemDetail(deviceId),null);
    }

    private static ProblemDetail buildProblemDetail(UUID deviceId){
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,"Device: "+deviceId+
                "is an AI agent; menage it through /api/v1/agent");
        pd.setTitle("Operation not allowed on AI Agent");
        return pd;
    }
}
