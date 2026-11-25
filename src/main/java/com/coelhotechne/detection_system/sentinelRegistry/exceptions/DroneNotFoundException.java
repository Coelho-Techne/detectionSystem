package com.coelhotechne.detection_system.sentinelRegistry.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.UUID;

@Getter
public class DroneNotFoundException extends ErrorResponseException {
    private final UUID droneId;
    public DroneNotFoundException(UUID droneId) {
        super(HttpStatus.NOT_FOUND, buildProblemDetail(droneId),null);
        this.droneId = droneId;
    }

    private static ProblemDetail buildProblemDetail(UUID droneId){
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,"Drone "+droneId);
        pd.setTitle("Drone Not Found");
        pd.setProperty("droneId",droneId);
        return pd;
    }
}
