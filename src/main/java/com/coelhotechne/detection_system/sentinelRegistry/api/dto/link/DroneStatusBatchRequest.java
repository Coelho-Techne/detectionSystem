package com.coelhotechne.detection_system.sentinelRegistry.api.dto.link;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record DroneStatusBatchRequest(
        @NotNull @Size(min = 1, max = DroneLinkLimits.MAX_BATCH)
        List<@Valid @NotNull DroneStatusReportItem>reports
) {
}
