package com.coelhotechne.detection_system.security.config.device;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

public class SensorDeviceAuthenticationToken extends DeviceAuthenticationToken{
    public SensorDeviceAuthenticationToken(UUID sensorId, String accessKey) {
        super(sensorId, accessKey);
    }

    public SensorDeviceAuthenticationToken(Object principal, UUID sensorId, Collection<? extends GrantedAuthority> authorities) {
        super(principal, sensorId, authorities);
    }
}
