package com.coelhotechne.detection_system.alert.application;

import com.coelhotechne.detection_system.detection.event.DetectionRecordedEvent;
import com.coelhotechne.detection_system.sensor.domain.Sensor;
import com.coelhotechne.detection_system.sensor.event.SensorPowerSupplyEvent;
import com.coelhotechne.detection_system.sensor.event.SensorStatusEvent;
import com.coelhotechne.detection_system.sensor.infrastructure.SensorRepository;
import com.coelhotechne.detection_system.telemetry.event.TelemetryThresholdEvent;
import com.coelhotechne.detection_system.zone.domain.Zone;
import com.coelhotechne.detection_system.zone.domain.enums.ZoneType;
import com.coelhotechne.detection_system.zone.infrastructure.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AlertRuleEngine {

    private final AlertWriter writer;
    private final ZoneRepository zoneRepository;
    private final SensorRepository sensorRepository;
    private final ZoneArmingPolicy armingPolicy;

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void onDetection(DetectionRecordedEvent event){
        ZoneType zoneType = Optional.ofNullable(event.zoneId())
                .flatMap(zoneRepository::findById)
                .map(Zone::getZoneType)
                .orElse(null);
        boolean armed = armingPolicy.isArmed(event.zoneId(),event.detectedAt());
        writer.apply(AlertRules.forDetection(event,zoneType,armed));
    }
    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void onSensorStatus(SensorStatusEvent event){
        Optional<Sensor> sensor = sensorRepository.findById(event.sensorId());
        writer.apply(AlertRules.forSensorStatus(event,
                sensor.map(Sensor::getSensorNiche).orElse(null),
                sensor.map(AlertRuleEngine::zoneIdOf).orElse(null)));
    }
    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void onPowerSupply(SensorPowerSupplyEvent event){
        UUID zoneId=sensorRepository.findById(event.sensorId()).map(AlertRuleEngine::zoneIdOf).orElse(null);
        writer.apply(AlertRules.forPower(event,zoneId));
    }
    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public  void onThreshold(TelemetryThresholdEvent event){
        writer.apply(AlertRules.forThreshold(event));
    }

    private static UUID zoneIdOf(Sensor sensor){
        return sensor.getZone() == null ? null: sensor.getZone().getUuid();
    }
}
