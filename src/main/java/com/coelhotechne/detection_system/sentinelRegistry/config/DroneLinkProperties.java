package com.coelhotechne.detection_system.sentinelRegistry.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@ConfigurationProperties(prefix = "drone.link")
public record DroneLinkProperties(
        Duration maxClockSkew,
        Duration maxBackfill,
        Duration reportRetention,
        Duration eventRetention
) {
    public DroneLinkProperties{
        maxClockSkew = maxClockSkew != null ? maxClockSkew : Duration.ofMinutes(5);
        maxBackfill = maxBackfill != null ? maxBackfill : Duration.ofDays(7);
        reportRetention = reportRetention != null ? reportRetention : Duration.ofDays(30);
        eventRetention = eventRetention != null ? eventRetention : Duration.ofDays(365);
        if (reportRetention.compareTo(maxBackfill) < 0 || eventRetention.compareTo(maxBackfill) < 0) {
            // Senão o lote atrasado é aceito e apagado na mesma madrugada
            throw new IllegalArgumentException
                    ("drone.link retention must be >= drone.link.max-backfill");
        }
    }


    @Configuration
    @EnableConfigurationProperties(DroneLinkProperties.class)
    static class Config{

    }
}
