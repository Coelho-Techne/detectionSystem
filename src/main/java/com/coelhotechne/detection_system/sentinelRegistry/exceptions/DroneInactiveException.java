package com.coelhotechne.detection_system.sentinelRegistry.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.UUID;

@Getter
public class DroneInactiveException extends ErrorResponseException {
    private final UUID droneId;
    public DroneInactiveException(UUID droneId) {
        super(HttpStatus.CONFLICT,buildProblemDetail(droneId),null);
        this.droneId = droneId;
    }

    private static ProblemDetail buildProblemDetail(UUID droneId){
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "Drone "+droneId);
        pd.setTitle("Drone Inactive");
        pd.setProperty("droneId",droneId);
        return pd;
    }
}
