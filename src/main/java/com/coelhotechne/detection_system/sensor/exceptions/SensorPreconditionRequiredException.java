package com.coelhotechne.detection_system.sensor.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

@Getter
public class SensorPreconditionRequiredException extends ErrorResponseException {

    private final String sensorId;

    public SensorPreconditionRequiredException(String sensorId) {
        super(HttpStatus.PRECONDITION_REQUIRED, buildProblemDetail(sensorId), null);
        this.sensorId = sensorId;
    }

    private static ProblemDetail buildProblemDetail(String sensorId) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.PRECONDITION_REQUIRED,
                "PUT requires the If-Match header with the sensor's read version. " +
                        "Perform a GET request, use the ETag from the response, and repeat the request..");
        pd.setTitle("Precondition Required");
        pd.setProperty("sensorId", sensorId);
        return pd;
    }
}
