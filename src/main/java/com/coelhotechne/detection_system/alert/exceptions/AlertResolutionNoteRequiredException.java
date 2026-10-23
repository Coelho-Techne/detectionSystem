package com.coelhotechne.detection_system.alert.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.UUID;

public class AlertResolutionNoteRequiredException extends ErrorResponseException {
    public AlertResolutionNoteRequiredException(UUID alertId) {
        super(HttpStatus.UNPROCESSABLE_CONTENT, buildProblemDetail(alertId), null);
    }

    private static ProblemDetail buildProblemDetail(UUID alertId) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT,
                "A resolution note is required for CRITICAL alerts");
        pd.setTitle("Resolution note required");
        pd.setProperty("alertId", String.valueOf(alertId));
        return pd;
    }
}
