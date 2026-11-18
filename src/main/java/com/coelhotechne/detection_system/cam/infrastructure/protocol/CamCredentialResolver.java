package com.coelhotechne.detection_system.cam.infrastructure.protocol;

public interface CamCredentialResolver {
    RtspCredentials resolve(String credentialRef,String targetHost);
}

