package com.coelhotechne.detection_system.sensor.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.UUID;

public class SensorHomologationNotEligibleException extends ErrorResponseException {
    public SensorHomologationNotEligibleException(UUID sensorId,String reason) {
        super(HttpStatus.CONFLICT,buildProblemDetail(sensorId,reason),null);
    }

    private static ProblemDetail buildProblemDetail(UUID sensorId, String reason){
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,reason);
        pd.setTitle("Sensor cannot eligible for this validation process");
        pd.setProperty("sensorId",sensorId);
        pd.setDetail(reason);
        return pd;
    }
}
