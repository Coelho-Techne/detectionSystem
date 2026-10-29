package com.coelhotechne.detection_system.security.config.device;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

public abstract class DeviceAccessKeyAuthenticationFilter extends OncePerRequestFilter{
    private static final String DEVICE_ID_HEADER = "X-Device-Id";
    private static final String ACCESS_KEY_HEADER = "X-Access-Key";

    private final AuthenticationManager authenticationManager;
    private final String pathPrefix;

    protected DeviceAccessKeyAuthenticationFilter(AuthenticationManager authenticationManager, String pathPrefix) {
        this.authenticationManager = authenticationManager;
        this.pathPrefix = pathPrefix;
    }

    protected abstract DeviceAuthenticationToken buildToken(UUID deviceId, String accessKey);

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (!request.getRequestURI().startsWith(pathPrefix)) {
            filterChain.doFilter(request, response);
            return;
        }

        String deviceIdHeader = request.getHeader(DEVICE_ID_HEADER);
        String accessKey = request.getHeader(ACCESS_KEY_HEADER);

        if (deviceIdHeader != null && accessKey != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UUID deviceId = UUID.fromString(deviceIdHeader);
                Authentication result = authenticationManager.authenticate(buildToken(deviceId, accessKey));
                SecurityContextHolder.getContext().setAuthentication(result);
            } catch (IllegalArgumentException e) {
                // X-Device-Id não é um UUID válido - não autentica, segue sem contexto.
            } catch (AuthenticationException e) {
                // accessKey inválida - idem, não estoura aqui.
            }
        }

        filterChain.doFilter(request, response);
    }
}
