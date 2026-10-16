package com.coelhotechne.detection_system.security.config.device;

import org.springframework.security.authentication.AuthenticationManager;

import java.util.UUID;

public class SensorAccessKeyAuthenticationFilter extends DeviceAccessKeyAuthenticationFilter {
    public SensorAccessKeyAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager, "/api/v1/sensor/");
    }

    @Override
    protected DeviceAuthenticationToken buildToken(UUID deviceId, String accessKey) {
        return new SensorDeviceAuthenticationToken(deviceId, accessKey);
    }
}
