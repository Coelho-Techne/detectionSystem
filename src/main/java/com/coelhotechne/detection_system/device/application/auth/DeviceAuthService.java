package com.coelhotechne.detection_system.device.application.auth;


import com.coelhotechne.detection_system.device.api.dto.auth.DeviceAuthResponse;
import com.coelhotechne.detection_system.globalClass.security.IssuedAccessKeyResponse;

import java.util.UUID;

public interface DeviceAuthService {
    DeviceAuthResponse authenticate(UUID uuid, String providedAccessKey);
    boolean isValidAccessKey(UUID uuid,String providedKey);
    IssuedAccessKeyResponse rotateAccessKey(UUID uuid);
}
