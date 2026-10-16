package com.coelhotechne.detection_system.sensor.application.event;

import com.coelhotechne.detection_system.batterysupply.domain.PowerSupply;
import com.coelhotechne.detection_system.batterysupply.domain.PowerSupplyThresholds;
import com.coelhotechne.detection_system.sensor.domain.Sensor;
import com.coelhotechne.detection_system.sensor.domain.SensorDiagnosticsThresholds;
import com.coelhotechne.detection_system.sensor.domain.enums.SensorNiche;
import com.coelhotechne.detection_system.sensor.domain.enums.SensorStatus;
import com.coelhotechne.detection_system.sensor.event.*;
import com.coelhotechne.detection_system.sensor.exceptions.SensorConcurrentModificationException;
import com.coelhotechne.detection_system.sensor.exceptions.SensorNotFoundException;
import com.coelhotechne.detection_system.sensor.infrastructure.SensorRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@Log4j2
public class SensorEventApplier {
    private final SensorRepository repository;
    private final ApplicationEventPublisher eventPublisher;
    private final SensorDiagnosticsThresholds thresholds;
    private final PowerSupplyThresholds powerSupplyThresholds;

    public SensorEventApplier(
            SensorRepository sensorRepository,
            ApplicationEventPublisher eventPublisher,
            SensorDiagnosticsThresholds thresholds,
            PowerSupplyThresholds powerSupplyThresholds
            ){
        this.repository=sensorRepository;
        this.eventPublisher=eventPublisher;
        this.thresholds=thresholds;
        this.powerSupplyThresholds=powerSupplyThresholds;
    }
    @Transactional
    public void apply(SensorEvent sensorEvent){
        if (sensorEvent instanceof SensorTelemetryEvent e) {
            applyTelemetry(e);
        } else if (sensorEvent instanceof SensorStatusReportedEvent e) {
            applyReportedStatus(e);
        } else if (sensorEvent instanceof SensorDetectionEvent e) {
            applyDetection(e);
        } else if (sensorEvent instanceof SensorStatusEvent e) {
            log.warn("SensorStatusEvent received as an input — it is an OUTPUT event (audit), " +
                    "issued by this very class: {}", e);
        } else {
            throw new IllegalStateException("Unhandled SensorEvent type: " + sensorEvent.getClass());
        }
    }

    private void applyTelemetry(SensorTelemetryEvent event) {
        Sensor sensor = requireSensor(event.sensorId());

        Sensor.DiagnosticsOutcome outcome = sensor.applyDiagnostics(
                event.diagnostics(),
                event.occurredAt(),
                thresholds);

        PowerSupply.PowerOutcome power= applyPowerReading(sensor,event);
        if (!outcome.persist() && !power.persist()) {
            Sensor saved = saveOrConflict(sensor);
            publishStatusChangeIfAny(saved.getUuid(), outcome.previousStatus(),
                    saved.getSensorStatus(), event.occurredAt());
            publishPowerChangeIfAny(saved.getUuid(),power,event.occurredAt());
        }
        eventPublisher.publishEvent(event);
    }

    private void publishPowerChangeIfAny(UUID sensorId, PowerSupply.PowerOutcome power, Instant ocurredAt){
        if (power.persist() && power.statusChanged()){
            eventPublisher.publishEvent(new SensorPowerSupplyEvent(
                    UUID.randomUUID(),
                    sensorId,
                    power.previousStatus(),
                    power.currentStatus(),
                    ocurredAt
            ));
        }
    }
    private PowerSupply.PowerOutcome applyPowerReading(Sensor sensor, SensorTelemetryEvent event) {
        PowerSupply power = sensor.getPowerSupply();
        if (power==null){
            log.debug("Sensor {} with not power supply - power reading ignored ",sensor.getUuid());
            return PowerSupply.PowerOutcome.unchanged(null);
        }
        if (!event.diagnostics().hasPowerReading()){
            return PowerSupply.PowerOutcome.unchanged(power.getStatus());
        }
        return power.applyReading(
                event.diagnostics().powerPercentage(),
                event.diagnostics().powerCharging(),
                event.occurredAt(),
                powerSupplyThresholds
        );
    }

    private void applyDetection(SensorDetectionEvent event) {
        Sensor sensor = requireSensor(event.sensorId());
        sensor.touchCommunication(event.occurredAt());
        eventPublisher.publishEvent(event);
    }

    private void applyReportedStatus(SensorStatusReportedEvent event) {
        Sensor sensor = requireSensor(event.sensorId());
        Sensor.DiagnosticsOutcome outcome =
                sensor.applyReportedStatus(event.reportedStatus(), event.occurredAt(), thresholds);
        if (!outcome.persist()) return;
        Sensor saved = saveOrConflict(sensor);
        publishStatusChangeIfAny(saved.getUuid(), outcome.previousStatus(),
                saved.getSensorStatus(), event.occurredAt());
    }

    private Sensor requireSensor(UUID uuid){
    return repository
            .findById(uuid)
            .orElseThrow(()-> new SensorNotFoundException(uuid.toString(),"Sensor not found!"));

    }

    private Sensor saveOrConflict(Sensor sensor){
        try {
            return repository.saveAndFlush(sensor);
        } catch (ObjectOptimisticLockingFailureException ex) {
            throw new SensorConcurrentModificationException(sensor.getUuid().toString(), ex);
        }
    }

    private void publishStatusChangeIfAny(UUID sensorId, SensorStatus previus, SensorStatus current, Instant occurredAt){
    if (previus!=current){
        eventPublisher.publishEvent(new SensorStatusEvent(UUID.randomUUID(),sensorId,previus,current,occurredAt));
    }
    }
}
