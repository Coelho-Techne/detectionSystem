package com.coelhotechne.detection_system.security.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class UserConflictException extends ErrorResponseException {

    public UserConflictException(HttpStatus status,String cause){
        super(status,buildProblemDetail(status,cause),null);
    }

    private static ProblemDetail buildProblemDetail(HttpStatus status, String cause){
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status,cause);
        pd.setTitle("User operation rejected");
        return pd;
    }
}
