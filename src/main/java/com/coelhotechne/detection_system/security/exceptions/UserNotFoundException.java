package com.coelhotechne.detection_system.security.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

@Getter
public class UserNotFoundException extends ErrorResponseException {
    private final String userId;
    private final String username;

    public UserNotFoundException(String userId,String username, String message){
        super(HttpStatus.NOT_FOUND,buildProblemDetail(userId,username,message),null);
        this.userId=userId;
        this.username=username;
    }

    private static ProblemDetail buildProblemDetail(String userId,String username,String message){
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,message);
        pd.setTitle("User Not Found");
        pd.setProperty("userId",userId);
        pd.setProperty("username",username);
        return pd;
    }
}