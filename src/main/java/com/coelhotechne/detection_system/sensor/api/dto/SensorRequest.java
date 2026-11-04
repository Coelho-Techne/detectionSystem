package com.coelhotechne.detection_system.sensor.api.dto;

import com.coelhotechne.detection_system.batterysupply.domain.PowerSupply;
import com.coelhotechne.detection_system.installation.api.dto.InstallationRequest;
import com.coelhotechne.detection_system.installation.domain.Installation;
import com.coelhotechne.detection_system.sensor.domain.enums.SensorNiche;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.util.UUID;

import static com.coelhotechne.detection_system.sensor.api.dto.SensorConstraints.NAME_MESSAGE;
import static com.coelhotechne.detection_system.sensor.api.dto.SensorConstraints.NAME_PATTERN;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SensorRequest(
        @NotBlank
        @Pattern(regexp = NAME_PATTERN, message = NAME_MESSAGE)
        String name,
        @NotNull
        SensorNiche sensorNiche,
        @NotBlank
        @Size(max = 255)
        String dataDescription,
        @NotNull
        UUID zoneUUID,
        @NotNull
        @Valid
        InstallationRequest installation,
        @Valid
        PowerSupply powerSupply
) {
}