package com.coelhotechne.detection_system.security.config.device;

import com.coelhotechne.detection_system.cam.application.auth.CamAuthService;
import com.coelhotechne.detection_system.cam.domain.base.BaseCam;
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
public class CamDeviceAuthenticationProvider implements AuthenticationProvider{
    private final CamAuthService camAuthService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        CamDeviceAuthenticationToken token = (CamDeviceAuthenticationToken) authentication;
        UUID camId = token.getDeviceId();
        String accessKey = (String) token.getCredentials();

        try {
            camAuthService.requireValidAccessKey(camId, accessKey);
        } catch (RuntimeException e) {
            throw new BadCredentialsException("Access key invalid for cam: " + camId, e);
        }

        return new CamDeviceAuthenticationToken(
                "CAM:" + camId, camId,
                List.of(new SimpleGrantedAuthority("DEVICE_CAM"))
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return CamDeviceAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
