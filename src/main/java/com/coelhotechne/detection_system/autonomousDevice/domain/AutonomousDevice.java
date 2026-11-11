package com.coelhotechne.detection_system.autonomousDevice.domain;

import com.coelhotechne.detection_system.autonomousDevice.domain.enums.AutonomousAgentStatus;
import com.coelhotechne.detection_system.autonomousDevice.domain.enums.AutonomyLevel;
import com.coelhotechne.detection_system.autonomousDevice.domain.enums.CommunicationStatus;
import com.coelhotechne.detection_system.autonomousDevice.domain.enums.ExecutionMode;
import com.coelhotechne.detection_system.device.domain.Device;
import com.coelhotechne.detection_system.security.domain.enums.Role;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "device")
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@EntityListeners(AuditingEntityListener.class)
public class AutonomousDevice extends Device {

    // Identification
    private String agentName;

    private String firmwareVersion;

    private String modelName;

    private String modelVersion;

    // Security
    @Enumerated(EnumType.STRING)
    private Role role;

    // Autonomy
    @Enumerated(EnumType.STRING)
    private AutonomyLevel autonomyLevel;

    @Enumerated(EnumType.STRING)
    private ExecutionMode executionMode;
    // Communication
    @Enumerated(EnumType.STRING)
    private CommunicationStatus communicationStatus;

    // Execution limits
    private Integer maxConcurrentTasks;

    private Integer maxRetries;

    private Integer executionTimeout;

    // Operational control
    private Boolean requireApproval;

    private Boolean allowAutomaticMaintenance;

    private Boolean allowAutomaticRecovery;

    // Monitoring
    @Enumerated(EnumType.STRING)
    private AutonomousAgentStatus agentStatus;

    private LocalDateTime lastExecution;

    private LocalDateTime lastHeartbeat;

    private Long totalExecutions;

    private Long failedExecutions;
}
