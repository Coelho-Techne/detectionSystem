package com.coelhotechne.detection_system.detection.application;

import com.coelhotechne.detection_system.detection.application.enums.DetectionOutcome;
import com.coelhotechne.detection_system.detection.domain.enums.DetectionDeviceType;
import com.coelhotechne.detection_system.detection.domain.enums.DetectionType;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class DetectionMetrics {

    private final MeterRegistry registry;

    public DetectionMetrics(MeterRegistry registry){
        this.registry = registry;
    }

    public void count(DetectionDeviceType deviceType, DetectionType type, DetectionOutcome outcome){
        Counter
                .builder("detection.recorded")
                .description("detections received, by recording result")
                .tag("device_type",deviceType == null ? "unknow" :deviceType.name().toLowerCase())
                .tag("type",type == null ? "none" : type.name().toLowerCase())
                .tag("result", outcome.tag()).register(registry).increment();
    }
}
