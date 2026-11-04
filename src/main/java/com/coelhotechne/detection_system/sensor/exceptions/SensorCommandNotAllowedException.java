package com.coelhotechne.detection_system.sensor.exceptions;

import com.coelhotechne.detection_system.sensor.domain.enums.SensorCommand;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.UUID;

public class SensorCommandNotAllowedException extends ErrorResponseException {
    public SensorCommandNotAllowedException(UUID sensorId, SensorCommand command,String reason){
        super(HttpStatus.BAD_REQUEST,buildProblemDetail(sensorId,command,reason),null);
    }

    private static ProblemDetail buildProblemDetail(UUID sensorId,SensorCommand command,String reason){
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,reason);
        pd.setTitle("Command not allowed");
        pd.setProperty("sensorId",String.valueOf(sensorId));
        pd.setProperty("command",String.valueOf(command));
        return pd;
    }
}
