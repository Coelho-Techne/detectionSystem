package com.coelhotechne.detection_system.cam.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.UUID;

public class CamInvalidRequestException extends ErrorResponseException {

    public CamInvalidRequestException(UUID camId,String reason){
        super(HttpStatus.UNPROCESSABLE_CONTENT,buildProblemDetail(camId,reason),null);
    }

    private static ProblemDetail buildProblemDetail(UUID camId,String reason){
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT,reason);
        pd.setTitle("Invalid camera request");
        pd.setProperty("camId",String.valueOf(camId));
        return pd;
    }
}
