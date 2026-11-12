package com.coelhotechne.detection_system.autonomousDevice.api.dto;

import com.coelhotechne.detection_system.autonomousDevice.domain.AutonomousDevice;
import com.coelhotechne.detection_system.autonomousDevice.domain.enums.AutonomousAgentType;
import com.coelhotechne.detection_system.autonomousDevice.domain.enums.AutonomousCapability;
import com.coelhotechne.detection_system.autonomousDevice.domain.enums.AutonomyLevel;
import jakarta.validation.constraints.*;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.util.Set;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AgentRegisterRequest (
        @NotBlank
        @Size(max = AutonomousDevice.NAME_MAX)
        String agentName,
        @NotNull
        AutonomousAgentType agentType,
        @NotNull
        AutonomyLevel autonomyLevel,
        @NotNull
        Set<AutonomousCapability> capability,
        @Size(max = 80)
        String modelName,
        @Size(max = 40)
        String modelVersion,
        @Size(max = 40)
        String firmwareVersion,
        Boolean requireApproval,
        Boolean allowAutomaticMaintenance,
        Boolean allowAutomaticRecovery,
        @Min(1) @Max(64)
        Integer maxConcurrentTasks,
        @Min(0) @Max(10)
        Integer maxRetries,
        @Min(1) @Max(3600)
        Integer executionTimeoutSeconds
){
}
