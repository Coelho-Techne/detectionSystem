package com.coelhotechne.detection_system.device.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.UUID;

public class DeviceSubscriptionNotFoundException extends ErrorResponseException {

    public DeviceSubscriptionNotFoundException(UUID deviceId, UUID subscriptionId) {
        super(HttpStatus.NOT_FOUND, buildProblemDetail(deviceId, subscriptionId), null);
    }

    private static ProblemDetail buildProblemDetail(UUID deviceId, UUID subscriptionId) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "Subscription not found!");
        pd.setTitle("Subscription not found");
        pd.setProperty("deviceId", String.valueOf(deviceId));
        pd.setProperty("subscriptionId", String.valueOf(subscriptionId));
        return pd;
    }
}
