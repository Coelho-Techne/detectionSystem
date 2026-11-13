package com.coelhotechne.detection_system.security.config;

import com.coelhotechne.detection_system.security.application.CustomUserDetailsService;
import com.coelhotechne.detection_system.security.config.device.CamAccessKeyAuthenticationFilter;
import com.coelhotechne.detection_system.security.config.device.CamDeviceAuthenticationProvider;
import com.coelhotechne.detection_system.security.config.device.SensorAccessKeyAuthenticationFilter;
import com.coelhotechne.detection_system.security.config.device.SensorDeviceAuthenticationProvider;
import com.coelhotechne.detection_system.security.domain.enums.Role;
import com.coelhotechne.detection_system.security.domain.jwt.JwtAuthenticationFilter;
import com.coelhotechne.detection_system.security.domain.jwt.JwtTokenProvider;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final CustomUserDetailsService userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;
    private final CamDeviceAuthenticationProvider camDeviceAuthenticationProvider;
    private final SensorDeviceAuthenticationProvider sensorDeviceAuthenticationProvider;

    @Value("${springdoc.api-docs.enabled:true}")
    private boolean apiDocsEnabled;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationProvider authenticationProvider) {
        return new ProviderManager(List.of(
                authenticationProvider,
                camDeviceAuthenticationProvider,
                sensorDeviceAuthenticationProvider
        ));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,  AuthenticationManager authenticationManager) throws Exception {
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtTokenProvider,userDetailsService);
        CamAccessKeyAuthenticationFilter camDeviceFilter = new CamAccessKeyAuthenticationFilter(authenticationManager);
        SensorAccessKeyAuthenticationFilter sensorDeviceFilter = new SensorAccessKeyAuthenticationFilter(authenticationManager);

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> {
                    auth.dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                            .requestMatchers(HttpMethod.POST,"/api/v1/cam/*/auth", "/api/v1/sensor/*/auth").permitAll()
                            .requestMatchers(HttpMethod.POST,"/api/v1/device/*/authenticate").permitAll()
                            .requestMatchers(HttpMethod.POST,"/api/v1/agent/*/heartbeat").permitAll()
                            .requestMatchers("/ws","/ws/**").permitAll()
                            .requestMatchers(HttpMethod.GET,"/actuator/health","/actuator/health/**").permitAll()
                            .requestMatchers("/api/v1/cam/*/homologation/**", "/api/v1/sensor/*/homologation/**")
                            .hasAnyRole(Role.TECHNICIAN.name(), Role.ADMIN.name());

                    if (apiDocsEnabled) {
                        auth.requestMatchers("/swagger-ui.html","/swagger-ui/**", "/v3/api-docs/**").permitAll();
                    }

                    auth.anyRequest().authenticated();
                })
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(camDeviceFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(sensorDeviceFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}

