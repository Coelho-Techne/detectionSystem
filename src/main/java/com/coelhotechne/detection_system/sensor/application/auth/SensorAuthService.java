package com.coelhotechne.detection_system.sensor.application.auth;

import com.coelhotechne.detection_system.globalClass.security.IssuedAccessKeyResponse;
import com.coelhotechne.detection_system.sensor.domain.Sensor;

import java.util.Optional;
import java.util.UUID;

public interface SensorAuthService {
    Optional<Sensor>authenticate(UUID uuid,String rawAccessKey);
    void requireValidAccessKey(UUID sensorId, String rawAccessKey);
    IssuedAccessKeyResponse issueAccessKey(UUID sensorId);
}
