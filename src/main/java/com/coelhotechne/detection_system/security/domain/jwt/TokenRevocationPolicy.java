package com.coelhotechne.detection_system.security.domain.jwt;

import com.coelhotechne.detection_system.security.domain.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public final class TokenRevocationPolicy {

    private TokenRevocationPolicy(){}

    public static boolean isRevoked(Date issuedAt, UserDetails userDetails){
        if (!(userDetails instanceof CustomUserDetails details)||details.getCredentialsChangedAt()==null){
            return false;
        }
        if (issuedAt ==null){
            return true;
        }
        Instant changeAt = details.getCredentialsChangedAt().truncatedTo(ChronoUnit.SECONDS);
        return issuedAt.toInstant().isBefore(changeAt);
    }
}
