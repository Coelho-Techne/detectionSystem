package com.coelhotechne.detection_system.security.config.device;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.UUID;

public abstract class DeviceAuthenticationToken extends AbstractAuthenticationToken {
    private final Object principal;
    private final String credentials;
    private final UUID deviceId;

    protected DeviceAuthenticationToken(UUID deviceId, String accessKey) {
        super((Collection<? extends GrantedAuthority>) null);
        this.principal = deviceId;
        this.credentials = accessKey;
        this.deviceId = deviceId;
        setAuthenticated(false);
    }

    // Estado pós-autenticação: construído pelo AuthenticationProvider depois de validar.
    protected DeviceAuthenticationToken(Object principal, UUID deviceId, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.credentials = null;
        this.deviceId = deviceId;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return credentials;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    public UUID getDeviceId() {
        return deviceId;
    }
}
