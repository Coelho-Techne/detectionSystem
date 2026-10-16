package com.coelhotechne.detection_system.sensor.domain.payload;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.tomcat.util.Diagnostics;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SensorTelemetryPayload (
        Integer version,
        UUID msgId,
        Instant measuredAt,
        Boolean status,
        BigDecimal memoryUsed,
        BigDecimal dataTransferValue,
        String dataDescription,
        BigDecimal powerPercentage,
        Boolean powerCharging,
        Integer rssiDbm,
        List<SensorReadingPayload> reading
){
    public SensorTelemetryPayload{
        reading = reading == null ? List.of() : List.copyOf(reading);
    }
    @JsonCreator
    static SensorTelemetryPayload fromJson(
            @JsonProperty("v")
            Integer version,
            @JsonProperty("msg_id")
            UUID msgId,
            @JsonProperty("measured_at")
            Instant measuredAt,
            @JsonProperty("readings")
            List<SensorReadingPayload> readings,
            @JsonProperty("diagnostics")
            Diagnostics diagnostics,
            @JsonProperty("status")
            Boolean status,
            @JsonProperty("memory_used") @JsonAlias("memoru_used")
            BigDecimal memoryUsed,
            @JsonProperty("data_transfer_value")
            BigDecimal dataTransferValue,
            @JsonProperty("data_description")
            String dataDescription,
            @JsonProperty("power_percentage") @JsonAlias("power_percentege")
            BigDecimal powerPercentage,
            @JsonProperty("power_charging")
            Boolean powerCharging,
            @JsonProperty("rssi_dbm")
            Integer rssiDbm
    ){
        Diagnostics d = diagnostics != null ? diagnostics :
                new Diagnostics(status, memoryUsed, dataTransferValue, dataDescription, powerPercentage, powerCharging, rssiDbm);

        return new SensorTelemetryPayload(
                version,
                msgId,
                measuredAt,
                status,
                memoryUsed,
                dataTransferValue,
                dataDescription,
                powerPercentage,
                powerCharging,
                rssiDbm,
                readings
        );
    }

    public boolean hasPowerReading() {
        return powerPercentage != null || powerCharging != null;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Diagnostics(
            @JsonProperty("status")
            Boolean status,
            @JsonProperty("memory_used") @JsonAlias("memoru_used")
            BigDecimal memoryUsed,
            @JsonProperty("data_transfer_value")
            BigDecimal dataTransferValue,
            @JsonProperty("data_description")
            String dataDescription,
            @JsonProperty("power_percentage") @JsonAlias("power_percentege")
            BigDecimal powerPercentage,
            @JsonProperty("power_charging")
            Boolean powerCharging,
            @JsonProperty("rssi_dbm")
            Integer rssiDbm
    ){}
}
