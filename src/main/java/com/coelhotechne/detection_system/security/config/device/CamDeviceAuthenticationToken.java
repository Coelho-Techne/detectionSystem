package com.coelhotechne.detection_system.security.config.device;

import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

public class CamDeviceAuthenticationToken extends DeviceAuthenticationToken{
    public CamDeviceAuthenticationToken(UUID camId, String accessKey) {
        super(camId, accessKey);
    }
    public CamDeviceAuthenticationToken(Object principal, UUID camId, Collection<? extends GrantedAuthority> authorities) {
        super(principal, camId, authorities);
    }
}
