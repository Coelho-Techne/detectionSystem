package com.coelhotechne.detection_system.sensor.mqtt;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("mqtt")
@RequiredArgsConstructor
public class MqttHealthIndicator implements HealthIndicator {
    private final MqttSensorClient client;

    @Value("${sensor.mqtt.max-silence-seconds:300}")
    private long maxSilenceSeconds;

    @Override
    public Health health() {
        long silence = client.secondsSinceLastMessage();

        Health.Builder builder = (client.isConnected()
                && client.isSubscriptionHealthy()
                && (silence < 0 || silence <= maxSilenceSeconds))
                ? Health.up()
                : Health.down();

        return builder
                .withDetail("connected", client.isConnected())
                .withDetail("subscriptionHealthy", client.isSubscriptionHealthy())
                // -1 = nenhuma mensagem desde a subida. Não é falha num sistema recém-iniciado,
                // por isso não derruba o status — mas aparece no detalhe.
                .withDetail("secondsSinceLastMessage", silence)
                .build();
    }
}
