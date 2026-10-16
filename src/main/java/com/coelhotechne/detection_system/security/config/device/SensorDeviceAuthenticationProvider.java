package com.coelhotechne.detection_system.security.config.device;

import com.coelhotechne.detection_system.sensor.application.auth.SensorAuthService;
import com.coelhotechne.detection_system.sensor.domain.Sensor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SensorDeviceAuthenticationProvider implements AuthenticationProvider {
    private final SensorAuthService sensorAuthService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SensorDeviceAuthenticationToken token = (SensorDeviceAuthenticationToken) authentication;
        UUID sensorId = token.getDeviceId();
        String accessKey = (String) token.getCredentials();
        Sensor sensor = sensorAuthService.authenticate(sensorId, accessKey)
                .orElseThrow(() -> new BadCredentialsException("Access key inválida para o sensor " + sensorId));
        return new SensorDeviceAuthenticationToken(
                "SENSOR:" + sensor.getUuid(), sensor.getUuid(),
                List.of(new SimpleGrantedAuthority("DEVICE_SENSOR"))
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SensorDeviceAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
