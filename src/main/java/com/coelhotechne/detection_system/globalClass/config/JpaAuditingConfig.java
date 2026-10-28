package com.coelhotechne.detection_system.globalClass.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.auditing.DateTimeProvider;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware",dateTimeProviderRef = "auditingDateTimeProvider")
public class JpaAuditingConfig {
    static final String SYSTEM = "system";

    public AuditorAware<String>auditorAware(){
        return ()->{
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth==null||!auth.isAuthenticated()|| auth instanceof AnonymousAuthenticationToken){
                return Optional.of(SYSTEM);
            }
            return Optional.ofNullable(auth.getName()).or(()-> Optional.of(SYSTEM));
        };
    }

    public DateTimeProvider auditingDateTimeProvider(Clock clock){
        return () -> Optional.of(LocalDateTime.now(clock));
    }
}
