package com.coelhotechne.detection_system.sensor.domain.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SensorDetectionPayload(
        @JsonProperty("v")
        Integer version,
        @JsonProperty("event_id")
        UUID eventId,
        @JsonProperty("category")
        String category,
        @JsonProperty("description")
        String description,
        @JsonProperty("detected_at")
        Instant detectedAt,
        @JsonProperty("confidence")
        BigDecimal confidence,
        @JsonProperty("value")
        BigDecimal value,
        @JsonProperty("unit")
        String unit,
        @JsonProperty("gas")
        String gas
) {
    public SensorDetectionPayload(
            Integer version,
            UUID eventId,
            String category,
            String description,
            Instant detectedAt,
            BigDecimal confidence,
            BigDecimal value,
            String unit
    ){
        this(version,eventId,category,description,detectedAt,confidence,value,unit,null);
    }

    public boolean hasCategory(){
        return category != null && !category.isBlank();
    }
}
