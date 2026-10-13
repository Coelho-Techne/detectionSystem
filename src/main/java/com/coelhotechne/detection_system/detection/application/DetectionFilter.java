package com.coelhotechne.detection_system.detection.application;

import com.coelhotechne.detection_system.detection.domain.enums.DetectionDeviceType;
import com.coelhotechne.detection_system.detection.domain.enums.DetectionType;
import com.coelhotechne.detection_system.detection.exceptions.DetectionInvalidFilterException;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

public record DetectionFilter(
        UUID zoneId,
        DetectionDeviceType deviceType,
        DetectionType type,
        UUID sourceId,
        Instant from,
        Instant to

) {
    public static final Duration DEFAULT_WINDOW = Duration.ofDays(7);

    public DetectionFilter withDefaults(Instant now){
        Instant end = to != null? to:now;
        Instant start = from != null? from : end.minus(DEFAULT_WINDOW);
        if (start.isAfter(end)){
            throw new DetectionInvalidFilterException("'from' must be before 'to'");
        }
        return new DetectionFilter(zoneId,deviceType,type, sourceId,start,end);
    }

    public DetectionFilter withSource(DetectionDeviceType deviceType,UUID sourceId){
        return new DetectionFilter(zoneId,deviceType,type,sourceId,from,to);
    }
}
