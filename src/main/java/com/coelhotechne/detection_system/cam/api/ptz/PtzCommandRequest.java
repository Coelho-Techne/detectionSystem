package com.coelhotechne.detection_system.cam.api.ptz;

import com.coelhotechne.detection_system.cam.domain.ptz.enums.PtzAction;
import jakarta.validation.constraints.NotNull;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PtzCommandRequest(
        @NotNull
        PtzAction action,
        Float value
) {
}
