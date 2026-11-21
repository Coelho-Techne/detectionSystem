package com.coelhotechne.detection_system.alert.config;


import com.coelhotechne.detection_system.device.application.auth.DeviceAuthService;
import com.coelhotechne.detection_system.security.application.CustomUserDetailsService;
import com.coelhotechne.detection_system.security.domain.jwt.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class StompAuthChannelInterceptorTest {

    @Test
    void  deletedUserWithValidTokenIsRejectedAsUnknownUser(){
        JwtTokenProvider tokens = mock(JwtTokenProvider.class);
        CustomUserDetailsService users = mock(CustomUserDetailsService.class);
        Claims claims = mock(Claims.class);
        when(claims.getSubject()).thenReturn("gone@local");
        when(tokens.parse("tok")).thenReturn(Optional.of(claims));
        when(users.loadUserByUsername("gone@local")).thenThrow(new UsernameNotFoundException("User not found"));

        StompAuthChannelInterceptor interceptor =
                new StompAuthChannelInterceptor(
                        tokens,
                        users,
                        mock(DeviceAuthService.class),
                        mock(StompSessionRegistry.class)
                );

        StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
        accessor.setNativeHeader("Authorization","Bearer tok");
        accessor.setLeaveMutable(true);
        Message<byte[]> message = MessageBuilder.createMessage(new byte[0],accessor.getMessageHeaders());
        assertThatThrownBy(()-> interceptor.preSend(message,null))
                .isInstanceOf(MessageDeliveryException.class)
                .hasMessageContaining("Unknown user");
    }
    @Test
    void deviceSubscribeIsRejectedAfterKeyRotation(){
        DeviceAuthService authService = mock(DeviceAuthService.class);
        UUID devideId = UUID.randomUUID();
        when(authService.isValidAccessKey(devideId,"key")).thenReturn(true);
        when(authService.activeKeyFingerprint(devideId)).thenReturn(Optional.of("hash-v1"));
        StompSessionRegistry registry = mock(StompSessionRegistry.class);

        StompAuthChannelInterceptor interceptor = new StompAuthChannelInterceptor(
                mock(JwtTokenProvider.class),
                mock(CustomUserDetailsService.class),
                authService,
                registry
        );
        Map<String, Object> session =new HashMap<>();
        StompHeaderAccessor connect = StompHeaderAccessor.create(StompCommand.CONNECT);
        connect.setNativeHeader("X-Device-Id",devideId.toString());
        connect.setNativeHeader("X-Access-Key","key");
        connect.setSessionId("s1");
        connect.setSessionAttributes(session);
        connect.setLeaveMutable(true);
        interceptor.preSend(MessageBuilder.createMessage(new byte[0],connect.getMessageHeaders()),null);
        verify(registry).bind("s1",devideId.toString());
        assertThat(session).containsEntry(StompAuthChannelInterceptor.ATTR_KEY_FINGERPRINT,"hash-v1");

        String destination = "/topic/devices/"+devideId+"/alerts";
        interceptor.preSend(subscribe(devideId,destination,session),null);

        when(authService.activeKeyFingerprint(devideId)).thenReturn(Optional.of("hash-v2"));
        assertThatThrownBy(()-> interceptor.preSend(subscribe(devideId,destination,session),null))
                .isInstanceOf(MessageDeliveryException.class)
                .hasMessageContaining("revoked");

        when(authService.activeKeyFingerprint(devideId)).thenReturn(Optional.empty());
        assertThatThrownBy(()-> interceptor.preSend(subscribe(devideId,destination,session),null))
                .isInstanceOf(MessageDeliveryException.class);
    }

    private static Message<byte[]> subscribe(UUID deviceId, String destination,Map<String,Object>session){
        StompHeaderAccessor sub = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
        sub.setDestination(destination);
        sub.setSessionId("s1");
        sub.setSessionAttributes(session);
        sub.setUser(UsernamePasswordAuthenticationToken.authenticated(deviceId.toString(),
                null,
                List.of(new SimpleGrantedAuthority(StompAuthChannelInterceptor.DEVICE_AUTHORITY))));
        sub.setLeaveMutable(true);
        return MessageBuilder.createMessage(new byte[0],sub.getMessageHeaders());
    }
}
