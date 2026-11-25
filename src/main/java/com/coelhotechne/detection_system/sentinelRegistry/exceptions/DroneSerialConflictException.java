package com.coelhotechne.detection_system.sentinelRegistry.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class DroneSerialConflictException extends ErrorResponseException {
    public DroneSerialConflictException(String serialNumber) {
        super(HttpStatus.CONFLICT,buildProblemDetail(serialNumber),null);
    }

    private static ProblemDetail buildProblemDetail(String serialNumber){
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,"serial_number "+serialNumber+" is already registered");
        pd.setTitle("Drone Already Registered");
        return pd;
    }
}
