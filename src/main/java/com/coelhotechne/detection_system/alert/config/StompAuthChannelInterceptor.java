package com.coelhotechne.detection_system.alert.config;

import com.coelhotechne.detection_system.device.application.auth.DeviceAuthService;
import com.coelhotechne.detection_system.security.application.CustomUserDetailsService;
import com.coelhotechne.detection_system.security.domain.jwt.JwtTokenProvider;
import com.coelhotechne.detection_system.security.domain.jwt.TokenRevocationPolicy;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Log4j2
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {
    static final String DEVICE_AUTHORITY = "DEVICE_ALERT_RECEIVER";
    private static final String BEARER = "Bearer ";
    static final String ATTR_TOKEN_ISSUED_AT = "auth.tokenIssuedAt";
    static final String ATTR_KEY_FINGERPRINT = "auth.keyFingerprint";


    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService detailsService;
    private final DeviceAuthService deviceAuthService;
    private final StompSessionRegistry stompSessionRegistry;

    @Override
    public Message<?>preSend(Message<?>message, MessageChannel channel){
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message,StompHeaderAccessor.class);

        if (accessor == null|| accessor.getCommand() == null){
            return message;
        }

        StompCommand command = accessor.getCommand();

        if (command == StompCommand.CONNECT) {
            Authentication authentication = authenticate(accessor);
            accessor.setUser(authentication);
            stompSessionRegistry.bind(accessor.getSessionId(),authentication.getName());
        } else if (command == StompCommand.SUBSCRIBE) {
            requireAllowedDestination(accessor);
            requireStillAuthorized(accessor);
        } else if (command == StompCommand.SEND) {
            throw new MessageDeliveryException("Alert channel is read-only");
        }
        return message;
    }


    private Authentication authenticate(StompHeaderAccessor accessor){
        String authorization = accessor.getFirstNativeHeader("Authorization");
        if (authorization!= null&&authorization.startsWith(BEARER)){
            Claims claims = tokenProvider.parse(authorization.substring(BEARER.length()))
                    .orElseThrow(()-> new MessageDeliveryException("Invalid or expired token"));
            String email = claims.getSubject();
            try {
                UserDetails userDetails = detailsService.loadUserByUsername(email);
                if (!userDetails.isEnabled()){
                    throw new MessageDeliveryException("User disabled");
                }if (TokenRevocationPolicy.isRevoked(claims.getIssuedAt(),userDetails)){
                    throw new MessageDeliveryException("token revoked");
                }
                remember(accessor,ATTR_TOKEN_ISSUED_AT,claims.getIssuedAt());
                return UsernamePasswordAuthenticationToken.authenticated(userDetails,
                        null,userDetails.getAuthorities());
            }catch (UsernameNotFoundException ex ){
                throw new MessageDeliveryException("Unknown user");
            }
        }

        String deviceIdHeader = accessor.getFirstNativeHeader("X-Device-Id");
        String accessKey = accessor.getFirstNativeHeader("X-Access-Key");
        if (deviceIdHeader!=null&&accessKey!=null){
            UUID deviceId = parseUuid(deviceIdHeader);
            if (deviceId!=null&& deviceAuthService.isValidAccessKey(deviceId,accessKey)){
                remember(accessor,ATTR_KEY_FINGERPRINT,deviceAuthService.activeKeyFingerprint(deviceId).orElse(null));
                return UsernamePasswordAuthenticationToken.authenticated(
                        deviceId.toString(),null, List.of(new SimpleGrantedAuthority(DEVICE_AUTHORITY))
                );
            }
        }
        log.debug("STOMP CONNECT rejected: missing or invalid credentials");
        throw new MessageDeliveryException("Authentication required");
    }

    private void requireAllowedDestination(StompHeaderAccessor accessor){
        if (!(accessor.getUser() instanceof Authentication authentication)||!authentication.isAuthenticated()){
            throw new MessageDeliveryException("Not authenticated");
        }
        String destination = accessor.getDestination();
        if (destination==null|| !destination.startsWith("/topic/")){
            throw new MessageDeliveryException("Destination not allowed: "+ destination);
        }
        boolean isDevice = authentication
                .getAuthorities()
                .stream()
                .anyMatch(a -> DEVICE_AUTHORITY.equals(a.getAuthority()));
        if (isDevice && !destination.equals("/topic/devices/"+ authentication.getName()+"/alerts")){
            throw new MessageDeliveryException("A device can only subscribe to its own alerts");
        }
    }
    private void requireStillAuthorized(StompHeaderAccessor accessor){
        Authentication authentication = (Authentication) accessor.getUser();
        boolean isDevice = authentication.getAuthorities().stream()
                .anyMatch(a -> DEVICE_AUTHORITY.equals(a.getAuthority()));
        Map<String,Object>attributes = accessor.getSessionAttributes();
        if (isDevice){
            UUID deviceId = parseUuid(authentication.getName());
            Optional<String>current = deviceId == null ? Optional.empty(): deviceAuthService.activeKeyFingerprint(deviceId);
            Object atConnect = attributes == null ? null : attributes.get(ATTR_KEY_FINGERPRINT);
            if (current.isEmpty() || !(atConnect instanceof String fingerprint) ||
                    !Objects.equals(fingerprint,current.get())){
               throw new  MessageDeliveryException("Device credentials revoked");
            }
            return;
        }
        try {
            UserDetails userDetails = detailsService.loadUserByUsername(authentication.getName());
            Object issuedAt = attributes == null ? null : attributes.get(ATTR_TOKEN_ISSUED_AT);
            if (!userDetails.isEnabled() || (issuedAt instanceof Date iat && TokenRevocationPolicy.isRevoked(iat,userDetails))){
                throw new MessageDeliveryException("User credentials revoked");
            }
        }catch (UsernameNotFoundException ex){
            throw new MessageDeliveryException("Unknown user");
        }
    }

    private static void remember(StompHeaderAccessor accessor,String key, Object value){
        Map<String, Object>attributes = accessor.getSessionAttributes();
        if (attributes != null && value!=null){
            attributes.put(key,value);
        }
    }
    private static UUID parseUuid(String value){
        try {
            return UUID.fromString(value);
        }catch (IllegalArgumentException e){
            return null;
        }
    }

}
