package com.coelhotechne.detection_system.sensor.application.homologation;

public interface SensorSelfTestPublisher {
    /**
     * Publica SELF_TEST com o requestId para o sensor.
     * @throws SensorCommandDeliveryException se o broker não aceitar a mensagem
     */
    void publish(SensorSelfTestRequest request);
}
