package com.coelhotechne.detection_system.alert.exceptions;

import com.coelhotechne.detection_system.alert.domain.enums.AlertStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.UUID;

public class AlertTransitionNotAllowedException extends ErrorResponseException {
    private final UUID alertId;
    public AlertTransitionNotAllowedException(UUID uuid, AlertStatus current,String action) {
        super(HttpStatus.CONFLICT,buildProblemDetail(uuid,current,action),null);
        this.alertId=uuid;
    }

    private static ProblemDetail buildProblemDetail(UUID uuid, AlertStatus current,String action){
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "Cannot %s an alert in status %s"
                .formatted(action,current));
        pd.setTitle("Alert transition not allowed");
        pd.setProperty("uuid",String.valueOf(uuid));
        pd.setProperty("status",String.valueOf(current));
        return pd;
    }
}
