package com.coelhotechne.detection_system.security.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

@Getter
public class UserEmailNotFoundException extends ErrorResponseException {
    private final String email;
    public UserEmailNotFoundException(String email,String cause) {
        super(HttpStatus.NOT_FOUND,buildProblemDetail(email,cause),null);
        this.email = email;
    }

    private static ProblemDetail buildProblemDetail(String email, String cause){
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,cause);
        pd.setTitle("E-mail not found");
        pd.setProperty("email",email);
        return pd;
    }

}
