package com.coelhotechne.detection_system.security.domain.jwt;

import  com.coelhotechne.detection_system.security.application.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Log4j2
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String PREFIX = "Bearer ";

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header != null && header.startsWith(PREFIX)
        && SecurityContextHolder.getContext().getAuthentication() == null) {
                String token = header.substring(PREFIX.length());
                tokenProvider.parse(token).ifPresent(claims -> authenticate(claims.getSubject(),request));
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(String email, HttpServletRequest servletRequest){
    try{
    UserDetails userDetails= userDetailsService.loadUserByUsername(email);
    if (!userDetails.isEnabled() || !userDetails.isAccountNonLocked()){
        log.debug("Token valid for inactive or blocked account: {}", email);
        return;
    }
    UsernamePasswordAuthenticationToken authenticationToken =
            UsernamePasswordAuthenticationToken.authenticated(userDetails,
                    null,
                    userDetails.getAuthorities());
    authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(servletRequest));
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authenticationToken);
        SecurityContextHolder.setContext(context);
    }catch (UsernameNotFoundException e){
    log.debug("Token valid for non existent user");
    }
    }
}
