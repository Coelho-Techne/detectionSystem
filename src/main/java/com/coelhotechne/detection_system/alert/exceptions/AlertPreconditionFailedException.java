package com.coelhotechne.detection_system.alert.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.UUID;

public class AlertPreconditionFailedException extends ErrorResponseException {
    public AlertPreconditionFailedException(UUID alertId, Long providedVersion, Long currentVersion) {
        super(HttpStatus.PRECONDITION_FAILED, buildProblemDetail(alertId, providedVersion, currentVersion), null);
    }

    private static ProblemDetail buildProblemDetail(UUID alertId, Long providedVersion, Long currentVersion) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.PRECONDITION_FAILED,
                "The alert has changed since the reading; reload and try again.");
        pd.setTitle("Precondition Failed");
        pd.setProperty("alertId", String.valueOf(alertId));
        pd.setProperty("providedVersion", providedVersion);
        pd.setProperty("currentVersion", currentVersion);
        return pd;
    }
}
