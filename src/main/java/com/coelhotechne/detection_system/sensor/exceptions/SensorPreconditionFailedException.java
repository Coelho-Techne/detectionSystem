package com.coelhotechne.detection_system.sensor.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

@Getter
public class SensorPreconditionFailedException extends ErrorResponseException {

    private final String sensorId;

    public SensorPreconditionFailedException(String sensorId, String providedVersion, Long currentVersion) {
        super(HttpStatus.PRECONDITION_FAILED, buildProblemDetail(sensorId, providedVersion, currentVersion), null);
        this.sensorId = sensorId;
    }

    private static ProblemDetail buildProblemDetail(String sensorId, String providedVersion, Long currentVersion) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.PRECONDITION_FAILED,
                "The sensor has changed since the reading; reload and edit again..");
        pd.setTitle("Precondition Failed");
        pd.setProperty("sensorId", sensorId);
        pd.setProperty("providedVersion", providedVersion);
        pd.setProperty("currentVersion", currentVersion);
        return pd;
    }
}