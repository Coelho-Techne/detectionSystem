package com.coelhotechne.detection_system.cam.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.UUID;

public class CamPtzCommandException extends ErrorResponseException {

    public CamPtzCommandException(UUID camId, Throwable cause){
        super(HttpStatus.BAD_GATEWAY,buildProblemDetail(camId,cause),null);
    }

    private static ProblemDetail buildProblemDetail(UUID camId,Throwable cause){
        ProblemDetail pd =ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY,
                "Camera did not accept the PTZ command"+cause.getMessage());
        pd.setTitle("PTZ command failed");
        pd.setProperty("camId",String.valueOf(camId));
        return pd;
    }
}
