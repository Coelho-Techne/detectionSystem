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
        String unit
) {
    public boolean hasCategory(){
        return category != null && !category.isBlank();
    }
}
