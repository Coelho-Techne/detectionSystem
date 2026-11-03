package com.coelhotechne.detection_system.device.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.UUID;

@Getter
public class DeviceAuthenticationException extends ErrorResponseException {
    private final UUID deviceId;

    public DeviceAuthenticationException(UUID deviceId) {
        super(HttpStatus.UNAUTHORIZED, buildProblemDetail(deviceId), null);
        this.deviceId = deviceId;
    }

    private static ProblemDetail buildProblemDetail(UUID deviceId) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED,
                "Access key invalid or device does not exist");
        pd.setTitle("Device authentication failed");
        pd.setProperty("deviceId", String.valueOf(deviceId));
        return pd;
    }
}
