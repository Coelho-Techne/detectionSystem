package com.coelhotechne.detection_system.alert.api.dto;

import jakarta.validation.constraints.Size;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AlertResolveRequest (
        @Size(max = 500)
        String note
){
}
