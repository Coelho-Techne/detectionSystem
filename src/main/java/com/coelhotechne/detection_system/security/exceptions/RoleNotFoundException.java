package com.coelhotechne.detection_system.security.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

@Getter
public class RoleNotFoundException extends ErrorResponseException {
    private final String role;
    public RoleNotFoundException(String role, String cause) {
        super(HttpStatus.NOT_FOUND,buildProblemDetail(role,cause),null);
        this.role=role;
    }

    private static ProblemDetail buildProblemDetail(String role,String cause){
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,cause);
        pd.setTitle("Role Not Found");
        pd.setProperty("role",role);
        return pd;
    }
}
