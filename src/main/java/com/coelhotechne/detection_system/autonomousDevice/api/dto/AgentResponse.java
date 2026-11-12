package com.coelhotechne.detection_system.autonomousDevice.api.dto;

import com.coelhotechne.detection_system.autonomousDevice.domain.AutonomousDevice;
import com.coelhotechne.detection_system.autonomousDevice.domain.enums.*;
import tools.jackson.databind.PropertyNamingStrategy;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

@JsonNaming(PropertyNamingStrategy.class)
public record AgentResponse(
        UUID uuid,
        Long version,
        String agentName,
        AutonomousAgentType agentType,
        AutonomyLevel autonomyLevel,
        ExecutionMode executionMode,
        AutonomousAgentStatus agentStatus,
        CommunicationStatus communicationStatus,
        Set<AutonomousCapability> capabilities,
        boolean requireApproval,
        boolean allowAutomaticMaintenance,
        boolean allowAutomaticRecovery,
        int maxConcurrentTasks,
        int maxRetries,
        int executionTimeoutSeconds,
        String modelName,
        String modelVersion,
        String firmwareVersion,
        Instant lastHeartbeatAt,
        Instant lastExecutionAt,
        long totalExecutions,
        long failedExecutions
) {
    public static AgentResponse from(AutonomousDevice device){
        Set<AutonomousCapability>cap =
                device.getCapabilities().isEmpty()
                        ? EnumSet.noneOf(AutonomousCapability.class)
                        : EnumSet.copyOf(device.getCapabilities());
        return new AgentResponse(
                device.getUuid(),
                device.getVersion(),
                device.getAgentName(),
                device.getAgentType(),
                device.getAutonomyLevel(),
                device.getExecutionMode(),
                device.getAgentStatus(),
                device.getCommunicationStatus(),
                device.getCapabilities(),
                device.isRequireApproval(),
                device.isAllowAutomaticMaintenance(),
                device.isAllowAutomaticRecovery(),
                device.getMaxConcurrentTasks(),
                device.getMaxRetries(),
                device.getExecutionTimeoutSeconds(),
                device.getModelName(),
                device.getModelVersion(),
                device.getFirmwareVersion(),
                device.getLastHeartbeatAt(),
                device.getLastExecutionAt(),
                device.getTotalExecutions(),
                device.getFailedExecutions()
        );
    }
}
