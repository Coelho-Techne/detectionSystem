package com.coelhotechne.detection_system.security.ratelimit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AuthRateLimiter {
    private static final Duration IP_WINDOW  =Duration.ofMinutes(1);
    private static final int CLEANUP_THRESHOLD = 10_000;

    private final boolean enabled;
    private final int perIpPerMinute;
    private final int loginMaxFailures;
    private final Duration loginLockout;
    private final Clock clock;


    private final Map<String, Window>ipWindows = new ConcurrentHashMap<>();
    private final Map<String, Failures>loginFailures = new ConcurrentHashMap<>();

    public AuthRateLimiter(@Value("${security.rate-limit.enabled:true}") boolean enabled,
                           @Value("${security.rate-limit.per-ip-per-minute:30}") int perIpPerMinute,
                           @Value("${security.rate-limit.login-max-failures:5}") int loginMaxFailures,
                           @Value("${security.rate-limit.login-lockout:15m}") Duration loginLockout,
                           Clock clock){
        this.enabled = enabled;
        this.perIpPerMinute = perIpPerMinute;
        this.loginMaxFailures = loginMaxFailures;
        this.loginLockout = loginLockout;
        this.clock = clock;
    }

    public boolean tryAcquireForIp(String ip){
        if (!enabled || ip ==null){
            return true;
        }
        Instant now = clock.instant();
        cleanupIfLarge(now);
        Window window = ipWindows.compute(ip,(k,w)->
                w == null || !now.isBefore(w.start().plus(IP_WINDOW))? new Window(now,1):
                new Window(w.start(), w.count()+1));
        return window.count()<=perIpPerMinute;
    }
    public boolean isLoginLocked(String email){
        if (!enabled || email == null){
            return false;
        }
        Failures f = loginFailures.get(normalize(email));
        return f != null && f.lockedUntil() != null && clock.instant().isBefore(f.lockedUntil());
    }
    public void recordLoginFailure(String email){
        if (!enabled|| email==null){
            return;
        }
        Instant now = clock.instant();
        loginFailures.compute(normalize(email),(k,f)->{
            int count = (f == null || (f.lockedUntil() != null && !now.isBefore(f.lockedUntil()))) ? 1 : f.count()+1;
            Instant lockedUntil = count >= loginMaxFailures ? now.plus(loginLockout) : null;
            return new Failures(count,lockedUntil,now);
        });
    }
    public void recordLoginSuccess(String email){
        if (email!=null){
            loginFailures.remove(normalize(email));
        }
    }
    private void cleanupIfLarge(Instant now){
        if (ipWindows.size()> CLEANUP_THRESHOLD){
            ipWindows.entrySet().removeIf(e-> !now.isBefore(e.getValue().start().plus(IP_WINDOW)));
        }if (loginFailures.size()> CLEANUP_THRESHOLD){
            loginFailures.entrySet().removeIf(e-> e.getValue().last().isBefore(now.minus(loginLockout)));
        }
    }
    private static String normalize(String email){
        return email.trim().toLowerCase(Locale.ROOT);
    }
    private record  Window(Instant start, int count){}
    private record Failures(int count, Instant lockedUntil, Instant last){}
}
