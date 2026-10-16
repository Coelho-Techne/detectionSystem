package com.coelhotechne.detection_system.sensor.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

@Getter
public class SensorPatchEmptyException extends ErrorResponseException {
    private final String sensorId;
    public SensorPatchEmptyException(String sensorId) {
        super(HttpStatus.BAD_REQUEST, buildProblemDetail(sensorId), null);
        this.sensorId = sensorId;
    }
    private static ProblemDetail buildProblemDetail(String sensorId) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                "The PATCH body does not contain any modifiable fields.. " +
                        "Accepted fields: name, data_description, zone_uuid, installation.");
        pd.setTitle("Patch Empty");
        pd.setProperty("sensorId", sensorId);
        return pd;
    }
}