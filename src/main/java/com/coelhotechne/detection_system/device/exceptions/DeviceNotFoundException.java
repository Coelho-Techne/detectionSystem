package com.coelhotechne.detection_system.device.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

public class DeviceNotFoundException extends ErrorResponseException {
    private final String deviceId;
    private final String deviceName;

    public DeviceNotFoundException(String deviceId,String deviceName,String message){
        super(HttpStatus.NOT_FOUND,buildProblemDetail(deviceId,deviceName,message),null);
        this.deviceId=deviceId;
        this.deviceName=deviceName;
    }

    private static ProblemDetail buildProblemDetail(String deviceId,String deviceName, String cause){
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,cause);
        pd.setTitle("Device Not Found");
        pd.setProperty("deviceId",deviceId);
        pd.setProperty("deviceName",deviceName);
        return pd;
    }
}
