package com.coelhotechne.detection_system.sentinelRegistry.api.dto.link;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record DroneCouplingRequest(
        @NotBlank @Size(max = 200)
        String accessKey
) {
}
