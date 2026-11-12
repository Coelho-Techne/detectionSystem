package com.coelhotechne.detection_system.autonomousDevice.domain;

import com.coelhotechne.detection_system.autonomousDevice.domain.enums.*;
import com.coelhotechne.detection_system.device.domain.Device;
import com.coelhotechne.detection_system.security.domain.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Entity
@Getter
@Table(name = "autonomous_device")
@PrimaryKeyJoinColumn(name = "uuid")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(callSuper = true,onlyExplicitlyIncluded = true)
public class AutonomousDevice extends Device {

    public static final int NAME_MAX = 80;

    // Identificação
    @Column(name = "agent_name", nullable = false, unique = true, length = NAME_MAX)
    private String agentName;

    @Enumerated(EnumType.STRING)
    @Column(name = "agent_type", nullable = false, length = 40)
    private AutonomousAgentType agentType;

    @Column(name = "model_name", length = 80)
    private String modelName;

    @Column(name = "model_version", length = 40)
    private String modelVersion;

    @Column(name = "firmware_version", length = 40)
    private String firmwareVersion;

    // Teto de permissões do próprio agente (o solicitante ainda limita por cima)
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "autonomous_device_capability", joinColumns = @JoinColumn(name = "agent_uuid"))
    @Enumerated(EnumType.STRING)
    @Column(name = "capability", nullable = false, length = 40)
    private Set<AutonomousCapability> capabilities = new HashSet<>();

    // Autonomia
    @Enumerated(EnumType.STRING)
    @Column(name = "autonomy_level", nullable = false, length = 20)
    private AutonomyLevel autonomyLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "execution_mode", nullable = false, length = 20)
    private ExecutionMode executionMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "communication_status", nullable = false, length = 20)
    private CommunicationStatus communicationStatus;

    // Limites de execução (aplicados pelo orquestrador de IA; aqui ficam a configuração e a auditoria)
    @Column(name = "max_concurrent_tasks", nullable = false)
    private int maxConcurrentTasks;

    @Column(name = "max_retries", nullable = false)
    private int maxRetries;

    @Column(name = "execution_timeout_seconds", nullable = false)
    private int executionTimeoutSeconds;

    // Controle operacional
    @Column(name = "require_approval", nullable = false)
    private boolean requireApproval;

    @Column(name = "allow_automatic_maintenance", nullable = false)
    private boolean allowAutomaticMaintenance;

    @Column(name = "allow_automatic_recovery", nullable = false)
    private boolean allowAutomaticRecovery;

    // Monitoramento
    @Enumerated(EnumType.STRING)
    @Column(name = "agent_status", nullable = false, length = 20)
    private AutonomousAgentStatus agentStatus;

    @Column(name = "last_execution_at")
    private Instant lastExecutionAt;

    @Column(name = "last_heartbeat_at")
    private Instant lastHeartbeatAt;

    @Column(name = "total_executions", nullable = false)
    private long totalExecutions;

    @Column(name = "failed_executions", nullable = false)
    private long failedExecutions;


    public static AutonomousDevice register(
            String agentName,
            AutonomousAgentType agentType,
            AutonomyLevel autonomyLevel,
            Set<AutonomousCapability>capabilities,
            String accessKeyHash,
            Instant now
    ){
        String name = Objects.requireNonNull(agentName,"agentName is required");
        if (name.isEmpty()||name.length()>NAME_MAX){
            throw new IllegalArgumentException("agentName must have 1.."+NAME_MAX+" characters");
        }
        AutonomousDevice agent = new AutonomousDevice();
        agent.agentName = name;
        agent.agentType = Objects.requireNonNull(agentType,"agentType is required");
        agent.autonomyLevel= Objects.requireNonNull(autonomyLevel,"autonomyLevel is required");
        agent.executionMode = ExecutionMode.USER_REQUESTED;
        agent.communicationStatus = CommunicationStatus.UNKNOWN;
        agent.agentStatus = AutonomousAgentStatus.CREATED;
        agent.maxConcurrentTasks = 1;
        agent.maxRetries = 0;
        agent.executionTimeoutSeconds = 30;
        agent.requireApproval = true;
        agent.replaceCapabilities(capabilities);
        agent.setDeviceName(name);
        agent.setDeviceType("AI_AGENT");
        agent.setStatus(Boolean.TRUE);
        agent.setLastCommunication(LocalDateTime.ofInstant(Objects.requireNonNull(now,"now is required"), ZoneOffset.UTC));
        agent.issueAccessKey(accessKeyHash);
        return agent;
    }
    // Decisions


    public AgentDecision decide(Role requesterRole, AutonomousCapability capability){
        if (capability == null){
            return AgentDecision.denied(null,requesterRole,"capability is required");
        }
        if (requesterRole == null){
            return AgentDecision.denied(capability,null,"agent must act on behalf of an authenticated user with a role");
        }
        if (!isOperational()){
            return AgentDecision.denied(capability,requesterRole,"agent is "+agentStatus);
        }
        if( agentStatus == AutonomousAgentStatus.TESTING && capability.isMutating()){
            return AgentDecision.denied(capability,requesterRole,"agent in TESTING is read");
        }
        if (!AgentPermissionPolicy.permits(requesterRole,capability)){
            return AgentDecision.denied(capability,requesterRole,"role "+requesterRole+" is not allowed to "+capability);
        }
        if(!capability.isMutating()){
            return AgentDecision.allowed(capability,requesterRole);
        }
        return switch (autonomyLevel){
            case OBSERVER -> AgentDecision.denied(capability,requesterRole,"OBSERVER agents are read-only");
            case ASSISTED -> AgentDecision.approval(capability,requesterRole,"ASSISTED agents only propose changes");
            case SUPERVISED -> AgentDecision.approval(capability,requesterRole,"SUPERVISED agent need human approval");
            case AUTONOMOUS -> autonomousDecision(requesterRole,capability);
        };
    }

    private AgentDecision autonomousDecision(Role role,AutonomousCapability capability){
        if (requireApproval){
            return AgentDecision.approval(capability,role,"agent configured with require_approval");
        }
        if (capability == AutonomousCapability.SYSTEM_MAINTENANCE && !allowAutomaticMaintenance){
            return AgentDecision.approval(capability,role,"automatic maintenance disabled");
        }
        if (capability == AutonomousCapability.DEVICE_RESTART && allowAutomaticRecovery){
            return AgentDecision.approval(capability,role,"automatic recovery is disabled");
        }
        return AgentDecision.allowed(capability,role);
    }



    public boolean isOperational(){
        if (!Boolean.TRUE.equals(getStatus())){
            return false;
        }
        return switch (agentStatus){
            case ACTIVE, IDLE,BUSY,TESTING -> true;
            default -> false;
        };
    }

    // Life

    public void activate(){
        if (agentStatus == AutonomousAgentStatus.DISABLED){
            throw new IllegalArgumentException("a DISABLED agent cannot be reactivated; register a new one");
        }
        agentStatus   = AutonomousAgentStatus.ACTIVE;
        setStatus(Boolean.TRUE);
    }

    public void suspend(){
        agentStatus = AutonomousAgentStatus.SUSPENDED;
    }

    public void disable(){
        agentStatus = AutonomousAgentStatus.DISABLED;
        setStatus(Boolean.FALSE);
    }
    public void heartBeat(Instant at){
        Objects.requireNonNull(at,"at is required");
        if (lastHeartbeatAt == null || at.isAfter(lastExecutionAt)){
            lastHeartbeatAt = at;
            setLastCommunication(LocalDateTime.ofInstant(at,ZoneOffset.UTC));
        }
        communicationStatus = CommunicationStatus.CONNECTED;
    }

    public void recordExecution(Instant at, boolean success){
        Objects.requireNonNull(at,"at is required");
        totalExecutions ++;
        if (!success){
            failedExecutions++;
        }
        if (lastExecutionAt == null || at.isAfter(lastExecutionAt)){
            lastExecutionAt = at;
        }
    }


    // Configuration

    public void replaceCapabilities(Set<AutonomousCapability>newCapabilities){
        Objects.requireNonNull(newCapabilities,"capabilities are required");
        capabilities.clear();
        if (!newCapabilities.isEmpty()){
            capabilities.addAll(EnumSet.copyOf(newCapabilities));
        }
    }

    public Set<AutonomousCapability>getCapabilities(){
        return Collections.unmodifiableSet(capabilities);
    }
    public void changeAutonomy(AutonomyLevel level,boolean requireApproval){
        this.autonomyLevel = Objects.requireNonNull(level,"autonomyLevel is required");
        this.requireApproval = requireApproval;
    }

    public void changeAutomation(boolean allowAutomaticMaintenance, boolean allowAutomaticRecovery){
        this.allowAutomaticMaintenance = allowAutomaticMaintenance;
        this.allowAutomaticRecovery = allowAutomaticRecovery;
    }
    public void changeExecutionLimits(int maxConcurrentTasks, int maxRetries, int executionTimeoutSeconds){
        if (maxConcurrentTasks< 1|| maxRetries < 0 || executionTimeoutSeconds < 1){
            throw new IllegalArgumentException("max_concurrent_tasks >= 1, max_retries >= 0, timeout >= 1s");
        }
        this.maxConcurrentTasks = maxConcurrentTasks;
        this.maxRetries = maxRetries;
        this.executionTimeoutSeconds = executionTimeoutSeconds;
    }
    public void describeModel(String modelName, String modelVersion, String firmwareVersion){
        this.modelName = modelName;
        this.modelVersion = modelVersion;
        this.firmwareVersion = firmwareVersion;
    }

    public void changeExecutionMode(ExecutionMode mode){
        this.executionMode = Objects.requireNonNull(mode,"executionMode is required");
    }


}
