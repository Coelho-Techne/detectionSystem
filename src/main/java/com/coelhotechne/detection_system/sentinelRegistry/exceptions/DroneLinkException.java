package com.coelhotechne.detection_system.sentinelRegistry.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.UUID;

public class DroneLinkException extends ErrorResponseException {
    public DroneLinkException(HttpStatus status,String title, String detail, UUID droneId) {
        super(status,buildProblemDetail(status,title,detail,droneId),null);
    }

    public static DroneLinkException invalidDroneKey(UUID droneId){
        return new DroneLinkException(HttpStatus.FORBIDDEN,"Drone Coupling Denied","The drone access key does not match",droneId);
    }

    public static DroneLinkException coupledElsewhere(UUID droneId){
        return new DroneLinkException(HttpStatus.CONFLICT,"Drone Coupled Elsewhere","Drone is coupled to another controller; decouple it first",droneId);
    }

    public static DroneLinkException notCoupled(UUID droneId){
        return new DroneLinkException(HttpStatus.FORBIDDEN, "Drone Not Coupled","Drone is not coupled to this controller",droneId);
    }

    public static DroneLinkException outOfWindow(UUID droneId,String field, int index,String reason){
        DroneLinkException ex = new DroneLinkException(HttpStatus.UNPROCESSABLE_CONTENT, "Drone data rejected",field + "[" + index + "]: " + reason,droneId);
        ex.getBody().setProperty("index",index);
        return ex;
    }

    private static ProblemDetail buildProblemDetail(HttpStatus status, String title, String detail, UUID droneId){
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status,detail);
        pd.setTitle(title);
        pd.setProperty("droneId",droneId);
        return pd;
    }
}
