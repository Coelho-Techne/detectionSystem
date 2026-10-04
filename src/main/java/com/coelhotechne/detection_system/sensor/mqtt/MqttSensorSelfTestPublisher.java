package com.coelhotechne.detection_system.sensor.mqtt;

import com.coelhotechne.detection_system.sensor.application.homologation.SensorSelfTestPublisher;
import com.coelhotechne.detection_system.sensor.application.homologation.SensorSelfTestRequest;
import com.coelhotechne.detection_system.sensor.domain.enums.SensorCommand;
import com.coelhotechne.detection_system.sensor.domain.payload.SensorSelfTestCommandPayload;
import com.coelhotechne.detection_system.sensor.exceptions.SensorCommandDeliveryException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;



@Log4j2
@Component
@RequiredArgsConstructor
public class MqttSensorSelfTestPublisher implements SensorSelfTestPublisher {
    private static final int QOS = 1;
    private static final String COMMAND_TOPIC = "home/%s/%s/command";
    private final MqttSensorClient mqttClient;
    private final ObjectMapper objectMapper;   // o mesmo ObjectMapper usado pelo SensorEventFactory
    private final Clock clock;

    @Override
    public void publish(SensorSelfTestRequest request) {
        String topic = COMMAND_TOPIC.formatted(request.zoneName(),request.sensorName());
        byte[] payload = serializer(new SensorSelfTestCommandPayload(
                SensorCommand.SELF_TEST.wireValue(),
                request.requestId(),
                clock.instant()
        ));
        try {
            mqttClient.publish(topic,payload,QOS);
        }catch (MqttException e){
            throw new SensorCommandDeliveryException(request.sensorId(),e);
        }
        log.debug("Self-test {} published to {}",request.requestId(),topic);
    }
    private byte[]serializer(SensorSelfTestCommandPayload payload){
        try {
            return objectMapper.writeValueAsBytes(payload);
        }catch (JacksonException e){
            throw new IllegalStateException("Could not serializer self-test command",e);
        }
    }
}
