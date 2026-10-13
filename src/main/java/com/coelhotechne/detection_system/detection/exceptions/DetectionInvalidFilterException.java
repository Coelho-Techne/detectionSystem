package com.coelhotechne.detection_system.detection.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class DetectionInvalidFilterException extends ErrorResponseException {

    public DetectionInvalidFilterException(String detail) {
        super(HttpStatus.BAD_REQUEST, buildProblemDetail(detail), null);
    }

    private static ProblemDetail buildProblemDetail(String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
        pd.setTitle("Invalid detection filter");
        return pd;
    }
}
