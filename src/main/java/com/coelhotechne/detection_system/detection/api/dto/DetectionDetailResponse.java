package com.coelhotechne.detection_system.detection.api.dto;

import com.coelhotechne.detection_system.sensor.domain.enums.SensorNiche;
import com.fasterxml.jackson.annotation.JsonInclude;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.math.BigDecimal;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DetectionDetailResponse(
        DetectionResponse detectionResponse,
        SensorBlock sensorBlock,
        CameraBlock cameraBlock
) {
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record SensorBlock(
            UUID uuid,
            String name,
            SensorNiche sensorNiche,
            BigDecimal measuredValue,
            String measuredUnit
    ){
    }

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public record CameraBlock(
            UUID uuid,
            String model,
            Long trackId,
            Boolean moving,
            String taxon,
            int[] bbox,
            String snapshotRef,
            String modelVersion
    ){
    }
}
