package com.coelhotechne.detection_system.security.api.dto;

import com.coelhotechne.detection_system.security.domain.enums.Role;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record UserUpdateRequest(
        Role role,
        Boolean enabled,
        @Size(max =  255)
        String fullName
) {
}
