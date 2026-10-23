package com.coelhotechne.detection_system.alert.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.UUID;

public class AlertNotFoundException extends ErrorResponseException {
    private final  UUID alertId;
    public AlertNotFoundException(UUID uuid) {
        super(HttpStatus.NOT_FOUND,buildProblemDetail(uuid),null);
        this.alertId=uuid;
    }

    private static ProblemDetail buildProblemDetail(UUID uuid){
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,"Alert not found!");
        pd.setTitle("Not found");
        pd.setProperty("uuid",String.valueOf(uuid));
        return pd;
    }
}
