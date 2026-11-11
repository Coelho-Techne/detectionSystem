package com.coelhotechne.detection_system.sensor.api.dto;

import com.coelhotechne.detection_system.installation.api.dto.InstallationRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.util.UUID;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SensorPatchRequest(
        @Pattern(regexp = "^[A-Za-z0-9_-]{1,15}$",
                message = "nome deve ter até 15 caracteres alfanuméricos, '-' ou '_' " +
                        "(é usado como segmento de tópico MQTT)")
        String name,
        @Size(min = 1, max = 255, message = "data_description cannot be null")
        String dataDescription,
        @Valid
        InstallationRequest installation,
        UUID zoneUUID
)
{
    public boolean isEmpty() {
        return name == null && dataDescription == null
                && zoneUUID == null && installation == null;
    }
}
