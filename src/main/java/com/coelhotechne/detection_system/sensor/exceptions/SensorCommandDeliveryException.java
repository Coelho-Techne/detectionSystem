package com.coelhotechne.detection_system.sensor.exceptions;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.net.URI;
import java.util.UUID;

public class SensorCommandDeliveryException extends ErrorResponseException {
    private static final URI TYPE = URI
            .create("https://coelhotechne.com/errors/sensor/command-delivery-failed");
    private static final String RETRY_AFTER_SECONDS = "5";
    public SensorCommandDeliveryException(UUID sensorId, Throwable cause) {
        super(HttpStatus.SERVICE_UNAVAILABLE,buildProblemDetail(sensorId),cause);
        getHeaders().set(HttpHeaders.RETRY_AFTER,RETRY_AFTER_SECONDS);
    }

    private static ProblemDetail buildProblemDetail(UUID sensorId){
        ProblemDetail pd = ProblemDetail.forStatus(HttpStatus.SERVICE_UNAVAILABLE);
        pd.setDetail("Not possible to publish the command for the %s sensor in broker MQTT."
                .formatted(sensorId));
        pd.setTitle("Sensor command delivery failed");
        pd.setType(TYPE);
        pd.setProperty("sensorId", sensorId);
        return pd;
    }
}
