package com.coelhotechne.detection_system.detection.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.UUID;

public class DetectionNotFoundException extends ErrorResponseException {

    private final UUID detectionId;

    public DetectionNotFoundException(UUID detectionId) {
        super(HttpStatus.NOT_FOUND, buildProblemDetail(detectionId), null);
        this.detectionId = detectionId;
    }

    public UUID getDetectionId() {
        return detectionId;
    }

    private static ProblemDetail buildProblemDetail(UUID detectionId) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Detection not found!");
        pd.setTitle("Detection not found!");
        pd.setProperty("detectionId", String.valueOf(detectionId));
        return pd;
    }
}