package com.coelhotechne.detection_system.zone.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

@Getter
public class ZoneInUseException extends ErrorResponseException {
    private final String id;

    public ZoneInUseException(String id, Long inUse){
        super(HttpStatus.CONFLICT,buildProblemDetail(id),null);
        this.id=id;
    }

    private static ProblemDetail buildProblemDetail(String id) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "The zone is in use");
        pd.setTitle("Zone Is In Use!");
        pd.setProperty("ID: ", id);
        return pd;
    }
}
