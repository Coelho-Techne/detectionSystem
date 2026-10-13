package com.coelhotechne.detection_system.detection.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.UUID;

public class DetectionSourceMismatchException extends ErrorResponseException {

    public DetectionSourceMismatchException(UUID pathCamId, UUID authenticatedCamId) {
        super(HttpStatus.FORBIDDEN, buildProblemDetail(pathCamId, authenticatedCamId), null);
    }

    private static ProblemDetail buildProblemDetail(UUID pathCamId, UUID authenticatedCamId) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN,
                "A camera can only send detections for itself");
        pd.setTitle("Detection source mismatch");
        pd.setProperty("camId", String.valueOf(pathCamId));
        pd.setProperty("authenticatedCamId", String.valueOf(authenticatedCamId));
        return pd;
    }
}
