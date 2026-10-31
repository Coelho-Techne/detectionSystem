package com.coelhotechne.detection_system.cam.application.auth;

import com.coelhotechne.detection_system.cam.api.dto.auth.CamAuthResponse;
import com.coelhotechne.detection_system.globalClass.security.IssuedAccessKeyResponse;

import java.util.UUID;
public interface CamAuthService {
    CamAuthResponse authenticate(UUID camId, String presentedAccessKey);
    void requireValidAccessKey(UUID camId, String presentedAccessKey);

    IssuedAccessKeyResponse issueAccessKey(UUID camId);
}