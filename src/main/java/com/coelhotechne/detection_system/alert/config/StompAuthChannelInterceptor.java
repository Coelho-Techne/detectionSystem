package com.coelhotechne.detection_system.alert.config;

import com.coelhotechne.detection_system.device.application.auth.DeviceAuthService;
import com.coelhotechne.detection_system.security.application.CustomUserDetailsService;
import com.coelhotechne.detection_system.security.domain.CustomUserDetails;
import com.coelhotechne.detection_system.security.domain.jwt.JwtTokenProvider;
import com.coelhotechne.detection_system.security.exceptions.UserNotFoundException;
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

import java.util.List;
import java.util.UUID;

@Component
@Log4j2
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {
    static final String DEVICE_AUTHORITY = "DEVICE_ALERT_RECEIVER";
    private static final String BEARER = "Bearer ";


    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final DeviceAuthService deviceAuthService;

    @Override
    public Message<?>preSend(Message<?>message, MessageChannel channel){
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message,StompHeaderAccessor.class);

        if (accessor == null|| accessor.getCommand() == null){
            return message;
        }

        StompCommand command = accessor.getCommand();

        if (command == StompCommand.CONNECT) {
            accessor.setUser(authenticate(accessor));
        } else if (command == StompCommand.SUBSCRIBE) {
            requireAllowedDestination(accessor);
        } else if (command == StompCommand.SEND) {
            throw new MessageDeliveryException("Alert channel is read-only");
        }
        return message;
    }


    private Authentication authenticate(StompHeaderAccessor accessor){
        String authorization = accessor.getFirstNativeHeader("Authorization");
        if (authorization!= null&&authorization.startsWith(BEARER)){
            String email = tokenProvider.parse(authorization.substring(BEARER.length()))
                    .map(claims -> claims.getSubject())
                    .orElseThrow(()-> new MessageDeliveryException("Invalid or expired token"));
            try {
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
                if (!userDetails.isEnabled()){
                    throw new MessageDeliveryException("User disabled");
                }
                return UsernamePasswordAuthenticationToken.authenticated(userDetails,
                        null,userDetails.getAuthorities());
            }catch (UserNotFoundException ex ){
                throw new MessageDeliveryException("Unknown user");
            }
        }

        String deviceIdHeader = accessor.getFirstNativeHeader("X-Device-Id");
        String accesskey = accessor.getFirstNativeHeader("X-Access-Key");
        if (deviceIdHeader!=null&&accesskey!=null){
            UUID deviceId = parseUuid(deviceIdHeader);
            if (deviceId!=null&& deviceAuthService.isValidAccessKey(deviceId,accesskey)){
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
            throw new MessageDeliveryException("A device can only subscribed to its own alerts");
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
