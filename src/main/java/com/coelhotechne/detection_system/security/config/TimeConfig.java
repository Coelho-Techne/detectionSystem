package com.coelhotechne.detection_system.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class TimeConfig {
    @Bean
    public static Clock clock(){
        return Clock.systemUTC();
    }
}
