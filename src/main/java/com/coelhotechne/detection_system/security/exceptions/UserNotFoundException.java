package com.coelhotechne.detection_system.security.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.UUID;

@Getter
public class UserNotFoundException extends ErrorResponseException {
    private final UUID userId;

    public UserNotFoundException(UUID userId, String message){
        super(HttpStatus.NOT_FOUND,buildProblemDetail(userId,message),null);
        this.userId=userId;
    }

    private static ProblemDetail buildProblemDetail(UUID userId,String message){
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,message);
        pd.setTitle("User Not Found");
        pd.setProperty("userId",userId);
        return pd;
    }
}