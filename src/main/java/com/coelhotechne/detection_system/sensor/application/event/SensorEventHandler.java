package com.coelhotechne.detection_system.sensor.application.event;

import com.coelhotechne.detection_system.sensor.application.auth.SensorAuthServiceImpl;
import com.coelhotechne.detection_system.sensor.domain.Sensor;
import com.coelhotechne.detection_system.sensor.event.SensorEvent;
import com.coelhotechne.detection_system.sensor.exceptions.SensorAuthenticationException;
import com.coelhotechne.detection_system.sensor.exceptions.SensorNotFoundException;
import com.coelhotechne.detection_system.sensor.infrastructure.SensorRepository;
import com.coelhotechne.detection_system.sensor.mqtt.RawSensorTopic;
import com.coelhotechne.detection_system.sensor.mqtt.SensorTopic;
import com.coelhotechne.detection_system.sensor.security.SensorPayloadSignature;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class SensorEventHandler {

    private final SensorEventFactory eventFactory;
    private final SensorRepository sensorRepository;
    private final SensorEventProcessor eventProcessor;
    private final boolean requireSignature;

    public SensorEventHandler(SensorEventFactory eventFactory,
                              SensorRepository sensorRepository,
                              SensorEventProcessor eventProcessor,
                              @Value("${sensor.mqtt.require-signature:false}") boolean requireSignature){
        this.eventFactory = eventFactory;
        this.sensorRepository = sensorRepository;
        this.eventProcessor = eventProcessor;
        this.requireSignature = requireSignature;
    }

    public void handle(String rawTopic, String payload) {
        RawSensorTopic raw = RawSensorTopic.parse(rawTopic);

        Sensor sensor = sensorRepository.findByNameAndZoneName(raw.sensorName(), raw.zoneName())
                .orElseThrow(() -> new SensorNotFoundException(raw.sensorName(),
                        "Sensor '%s' not found in zone '%s'".formatted(raw.sensorName(), raw.zoneName())));
        String body = authenticate(sensor,rawTopic,payload);
        SensorTopic topic = new SensorTopic(sensor.getUuid(), raw.eventType());
        SensorEvent event = eventFactory.create(topic, body);
        eventProcessor.process(event);
    }

    private String authenticate(Sensor sensor, String rawTopic, String payload){
        if (!SensorAuthServiceImpl.isAllowedToOperate(sensor)){
            throw new SensorAuthenticationException(sensor.getUuid().toString(),
                    "Sensor is out of operation (homologation rejected or removed); message discarded");
        }
        SensorPayloadSignature.Parsed parsed = SensorPayloadSignature.parse(payload);

        if (parsed.isSigned()){
            if (!SensorPayloadSignature.verify(sensor.getAccessKey(),rawTopic,parsed)){
                throw new SensorAuthenticationException(sensor.getUuid().toString(),
                        "Invalid MQTT payload signature");
            }
            return parsed.body();
        }
        if (requireSignature){
            throw new SensorAuthenticationException(sensor.getUuid().toString(),
                    "Unsigned MQTT payload rejected (sensor.mqtt.require-signature=true)");
        }
        return payload;
    }
}
