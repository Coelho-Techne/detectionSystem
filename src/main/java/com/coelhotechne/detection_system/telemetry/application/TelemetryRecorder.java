package com.coelhotechne.detection_system.telemetry.application;

import com.coelhotechne.detection_system.sensor.domain.Sensor;
import com.coelhotechne.detection_system.sensor.domain.payload.SensorReadingPayload;
import com.coelhotechne.detection_system.sensor.domain.payload.SensorTelemetryPayload;
import com.coelhotechne.detection_system.sensor.event.SensorTelemetryEvent;
import com.coelhotechne.detection_system.sensor.exceptions.SensorNotFoundException;
import com.coelhotechne.detection_system.sensor.infrastructure.SensorRepository;
import com.coelhotechne.detection_system.telemetry.domain.Telemetry;
import com.coelhotechne.detection_system.telemetry.domain.enums.TelemetryMetric;
import com.coelhotechne.detection_system.telemetry.domain.enums.TelemetrySource;
import com.coelhotechne.detection_system.telemetry.infrastructure.TelemetryRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Log4j2
public class TelemetryRecorder {
    private final TelemetryRepository repository;
    private final SensorRepository sensorRepository;
    private final TelemetryThresholdEvaluator thresholdEvaluator;
    private final MeterRegistry meterRegistry;
    private final Duration diagnosticsSampleInterval;

    private final Map<UUID, Instant>lastDiagnostics = new ConcurrentHashMap<>();

    public TelemetryRecorder(
            TelemetryRepository repository,
            SensorRepository sensorRepository,
            TelemetryThresholdEvaluator thresholdEvaluator,
            MeterRegistry meterRegistry,
            @Value("${telemetry.diagnostics.sample-interval:60s}")
            Duration diagnosticsSampleInterval
    ){
        this.repository = repository;
        this.sensorRepository = sensorRepository;
        this.thresholdEvaluator = thresholdEvaluator;
        this.meterRegistry = meterRegistry;
        this.diagnosticsSampleInterval = diagnosticsSampleInterval;
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void onTelemetry(SensorTelemetryEvent event){
        record(event);
    }

    List<Telemetry> record(SensorTelemetryEvent event){
        SensorTelemetryPayload payload = event.diagnostics();
        if (payload == null){
            return List.of();
        }

        UUID messegeId = event.hasDeviceMessageId() ? event.eventId() : null;
        if (messegeId != null && repository.existsBySensorUuidAndSourceMessageId(event.sensorId(),messegeId)){
            log.debug("Telemetry message {} from sensor {} already recorded (redelivery) - ignored",messegeId,event.sensorId());
            count("all","duplicate");
            return List.of();
        }
        Sensor sensor = sensorRepository.findById(event.sensorId()).orElseThrow(()-> new SensorNotFoundException(
                event.sensorId().toString(),"Sensor not found!"));

        List<Telemetry>rows = new ArrayList<>();

        for (SensorReadingPayload readingPayload : payload.reading()){
            addReading(rows,sensor,readingPayload,event,messegeId);
        }
        if (isDiagnosticsSampleDue(sensor.getUuid(),event.occurredAt())){
            addDiagnostics(rows, sensor,TelemetryMetric.MEMORY_USED,payload.memoryUsed(),event,messegeId);
            addDiagnostics(rows, sensor,TelemetryMetric.DATA_TRANSFER,payload.dataTransferValue(),event,messegeId);
            addDiagnostics(rows, sensor,TelemetryMetric.BATTERY_PERCENT,payload.powerPercentage(),event,messegeId);
            addDiagnostics(rows, sensor,TelemetryMetric.RSSI,
                    payload.rssiDbm() == null ? null : BigDecimal.valueOf(payload.rssiDbm())
                    ,event,messegeId);
        }
        if (rows.isEmpty()){
            return rows;
        }
        List<Telemetry>saved = repository.saveAll(rows);
        saved.forEach(t -> count(t.getMetric().name().toLowerCase(),"stored"));
        thresholdEvaluator.evaluete(saved);
        return saved;
    }

    private void addDiagnostics(List<Telemetry>rows, Sensor sensor, TelemetryMetric metric,
                                BigDecimal value, SensorTelemetryEvent event,UUID messageId){
        if (value!=null){
            rows.add(new Telemetry(
                    sensor,
                    metric,
                    value,
                    event.occurredAt(),
                    event.receivedAt(),
                    TelemetrySource.MQTT,
                    messageId
            ));
        }
    }
    private boolean isDiagnosticsSampleDue(UUID sensorId, Instant at){
        boolean[] due = {false};
        lastDiagnostics.compute(sensorId,(id,last)->{
            if (last == null||!at.isBefore(last.plus(diagnosticsSampleInterval))){
                due[0] = true;
                return at;
            }
            return last;
        });
        return due[0];
    }
    private void addReading(List<Telemetry> rows, Sensor sensor,SensorReadingPayload readingPayload,
                            SensorTelemetryEvent event,UUID messageId){
        TelemetryMetric metric = TelemetryMetric.fromCode(readingPayload.metric());

        if(metric == null||readingPayload.value()==null){
            log.warn("Sensor {} sent reading with metric '{}' or no value - discarded",sensor.getUuid(),readingPayload.metric());
            count("unknown","rejected");
            return;
        }if (!metric.allowsNiche(sensor.getSensorNiche())){
            log.warn("Sensor{} (niche{}) sent metric {}, not allowed for its niche - discarded",
                    sensor.getUuid(),sensor.getSensorNiche(),metric);
            count(metric.name().toLowerCase(),"rejected");
            return;
        }
        rows.add(
                new Telemetry(
                        sensor,
                        metric,
                        readingPayload.value(),
                        event.occurredAt(),
                        event.receivedAt(),
                        TelemetrySource.MQTT,
                        messageId
                )
        );
    }

    private void count(String metric, String result){
        meterRegistry.counter("detection.telemetry.readings", "metric",metric,"result",result).increment();
    }
}
