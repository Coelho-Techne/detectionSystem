package com.coelhotechne.detection_system.security.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

@Getter
public class RoleUnauthorizedException extends ErrorResponseException {

    private final String role;

    public RoleUnauthorizedException(String role, String cause){
        super(HttpStatus.UNAUTHORIZED,buildProblemDetail(role,cause),null);
        this.role=role;
    }

    private static ProblemDetail buildProblemDetail(String role, String cause){
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,cause);
        pd.setTitle("Unauthorized Role");
        pd.setProperty("role",role);
        return pd;
    }
}
