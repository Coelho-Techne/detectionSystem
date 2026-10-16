package com.coelhotechne.detection_system.sensor.domain.payload;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SensorReadingPayload (
        @JsonProperty("metric")
        String metric,
        @JsonProperty("value")
        BigDecimal value,
        @JsonProperty("unit")
        String unit
){

}
