package com.coelhotechne.detection_system.security.domain.jwt;

import com.coelhotechne.detection_system.security.domain.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Component
public class JwtTokenProvider {
    private static final String ROLE_CLAIM = "role";
    private final JwtProperties jwtProperties;
    private final Clock clock;
    private final SecretKey key;

    public JwtTokenProvider(JwtProperties jwtProperties,Clock clock){
        this.jwtProperties=jwtProperties;
        this.clock=clock;
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }
    public String generateToken(String subjectEmail, Role role) {
        Instant now = Instant.now(clock);
        Instant expiresAt = now.plus(jwtProperties.getExpiration());

        return Jwts.builder()
                .setSubject(subjectEmail)
                .claim(ROLE_CLAIM, role.name())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(expiresAt))
                .signWith(key)
                .compact();
    }
    public Optional<Claims> parse(String token) {
        try {
            return Optional.of(Jwts.parserBuilder()
                    .setClock(() -> Date.from(Instant.now(clock)))
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody());
        }catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
