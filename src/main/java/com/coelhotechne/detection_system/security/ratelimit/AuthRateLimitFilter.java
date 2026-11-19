package com.coelhotechne.detection_system.security.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.regex.Pattern;

@Log4j2
public class AuthRateLimitFilter extends OncePerRequestFilter {
    private static final Pattern PUBLIC_AUTH  = Pattern.compile(
            "^/api/v1/(auth/login|cam/[^/]+/auth|sensor/[^/]+/auth|device/[^/]+/authenticate|agent/[^/]+/heartbeat)/?$"
    );

    private final AuthRateLimiter limiter;

    public AuthRateLimitFilter(AuthRateLimiter limiter){
        this.limiter = limiter;
    }
    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request){
        return !HttpMethod.POST.matches(request.getMethod())
                || !PUBLIC_AUTH.matcher(request.getRequestURI().substring(request.getContextPath().length())).matches();
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)throws ServletException, IOException{
        if (!limiter.tryAcquireForIp(request.getRemoteAddr())){
            log.warn("Rate limit exceeded on {} from {}",request.getRequestURI(),request.getRemoteAddr());
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After","60");
            response.setContentType("application/problem+json");
            response.getWriter().write("{\"title\":\"Too many requests\",\"status\":429}");
            return;
        }
        chain.doFilter(request,response);
    }
}
