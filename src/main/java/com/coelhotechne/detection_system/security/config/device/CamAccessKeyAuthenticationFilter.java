package com.coelhotechne.detection_system.security.config.device;

import org.springframework.security.authentication.AuthenticationManager;

import java.util.UUID;

public class CamAccessKeyAuthenticationFilter extends DeviceAccessKeyAuthenticationFilter{

    public CamAccessKeyAuthenticationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager, "/api/v1/cam/");
    }
    @Override
    protected DeviceAuthenticationToken buildToken(UUID deviceId, String accessKey) {
        return new CamDeviceAuthenticationToken(deviceId, accessKey);
    }
}
