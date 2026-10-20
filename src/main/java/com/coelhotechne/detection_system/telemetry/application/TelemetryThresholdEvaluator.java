package com.coelhotechne.detection_system.telemetry.application;

import com.coelhotechne.detection_system.telemetry.domain.Telemetry;
import com.coelhotechne.detection_system.telemetry.domain.enums.TelemetryMetric;
import com.coelhotechne.detection_system.telemetry.event.TelemetryThresholdEvent.Band;
import com.coelhotechne.detection_system.telemetry.event.TelemetryThresholdEvent;
import com.coelhotechne.detection_system.telemetry.application.TelemetryThresholdProperties.Limit;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TelemetryThresholdEvaluator {

    private final TelemetryThresholdProperties propeties;
    private final ApplicationEventPublisher eventPublisher;
    private final Map<Key, TelemetryThresholdEvent.Band> bands = new ConcurrentHashMap<>();

    public TelemetryThresholdEvaluator(
            TelemetryThresholdProperties propeties,
            ApplicationEventPublisher eventPublisher
    ){
        this.propeties=propeties;
        this.eventPublisher=eventPublisher;
    }

    public void evaluete(List<Telemetry> readings){
        for (Telemetry reading : readings){
            Limit limit = propeties.limits().get(reading.getMetric());
            if (limit ==null){
                continue;
            }
            UUID sensorId  = reading.getSensor().getUuid();
            Key key = new Key(sensorId,reading.getMetric());
            Band previous = bands.get(key);
            Band current = classify(reading.getValue(),limit,previous);
            bands.put(key,current);
            if (previous == current){
                continue;
            }
            eventPublisher.publishEvent(new TelemetryThresholdEvent(
                    UUID.randomUUID(),
                    sensorId,
                    reading.getZone() == null ? null : reading.getZone().getUuid(),
                    reading.getMetric(),
                    reading.getValue(),
                    current == TelemetryThresholdEvent.Band.ABOVE ? limit.max() : current == TelemetryThresholdEvent.Band.BELOW ? limit.min() : null,
                    previous,
                    current,
                    reading.getMeasuredAt()
            ));
        }
    }

    static Band classify(BigDecimal value,Limit limit, Band previous){
        if (limit.max()!=null){
            if (value.compareTo(limit.max())>0){
                return Band.ABOVE;
            }
            if (previous == Band.ABOVE && value.compareTo(limit.max().subtract(limit.hysteresis()))>0){
                return Band.ABOVE;
            }
        }

        if (limit.min()!=null){
            if (value.compareTo(limit.min())<0){
                return Band.BELOW;
            }
            if (previous == Band.BELOW && value.compareTo(limit.min().subtract(limit.hysteresis()))<0){
                return Band.BELOW;
            }
        }
        return Band.NORMAL;
    }

    private record Key(UUID sensorId, TelemetryMetric metric){

    }
}
