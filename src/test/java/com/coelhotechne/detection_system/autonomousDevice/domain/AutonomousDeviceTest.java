package com.coelhotechne.detection_system.autonomousDevice.domain;


import com.coelhotechne.detection_system.autonomousDevice.domain.enums.AgentDecisionOutcome;
import com.coelhotechne.detection_system.autonomousDevice.domain.enums.AutonomousAgentType;
import com.coelhotechne.detection_system.autonomousDevice.domain.enums.AutonomousCapability;
import com.coelhotechne.detection_system.autonomousDevice.domain.enums.AutonomyLevel;
import com.coelhotechne.detection_system.security.domain.enums.Role;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.EnumSet;

import static com.coelhotechne.detection_system.autonomousDevice.domain.enums.AgentDecisionOutcome.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AutonomousDeviceTest {

    private static final Instant TO = Instant.parse("2026-11-12T13:00:00Z");

    private static AutonomousDevice agent(AutonomyLevel level, boolean requireApproval
    , AutonomousCapability... capabilities){
        AutonomousDevice device = AutonomousDevice.register("assistente",
                AutonomousAgentType.ALERT_MANAGEMENT,
                level,
                EnumSet.of(capabilities[0],capabilities),
                "hash",
                TO);
        device.changeAutonomy(level,requireApproval);
        device.activate();
        return device;
    }
    private static AgentDecisionOutcome outcome(AutonomousDevice device, Role role, AutonomousCapability capability){
        return device.decide(role,capability).outcome();
    }
    @Test
    void registeredAgentStartsInactiveAndConservative(){
        AutonomousDevice device = AutonomousDevice.register(" ia-1 ",AutonomousAgentType.GENERAL_PURPOSE,
                AutonomyLevel.AUTONOMOUS,EnumSet.of(AutonomousCapability.ALERT_READ),"hash",TO);

        assertThat(device.getAgentName()).isEqualTo("ia-1");
        assertThat(device.isRequireApproval()).isTrue();
        assertThat(device.isOperational()).isFalse();
        assertThat(outcome(device,Role.ADMIN,AutonomousCapability.ALERT_READ)).isEqualTo(DENIED);
        assertThat(device.getAccessKey()).isEqualTo("hash");
    }
    @Test
    void agentNeverExceedsRequesterRole(){
        AutonomousDevice device = agent(AutonomyLevel.AUTONOMOUS,false,AutonomousCapability.ALERT_READ,AutonomousCapability.ALERT_ACKNOWLEDGE,AutonomousCapability.DEVICE_UPDATE);
        assertThat(outcome(device,Role.ADMIN,AutonomousCapability.DEVICE_UPDATE)).isEqualTo(ALLOWED);
        assertThat(outcome(device,Role.TECHNICIAN,AutonomousCapability.DEVICE_UPDATE)).isEqualTo(ALLOWED);
        assertThat(outcome(device,Role.HOME_USER,AutonomousCapability.DEVICE_UPDATE)).isEqualTo(DENIED);
        assertThat(outcome(device,Role.HOME_USER,AutonomousCapability.ALERT_ACKNOWLEDGE)).isEqualTo(ALLOWED);
        assertThat(outcome(device,Role.AI_USER,AutonomousCapability.ALERT_ACKNOWLEDGE)).isEqualTo(DENIED);
        assertThat(outcome(device,Role.AI_USER,AutonomousCapability.ALERT_READ)).isEqualTo(ALLOWED);
        assertThat(outcome(device,Role.DEVELOPER,AutonomousCapability.ALERT_READ)).isEqualTo(DENIED);
        assertThat(device.decide(Role.HOME_USER,AutonomousCapability.DEVICE_UPDATE).reason()).contains("HOME_USER");
    }
    @Test
    void agentNeverExceedsItsOwnCapabilities(){
        AutonomousDevice device = agent(AutonomyLevel.AUTONOMOUS,false,AutonomousCapability.ALERT_READ);
        assertThat(outcome(device,Role.ADMIN,AutonomousCapability.ALERT_ACKNOWLEDGE)).isEqualTo(DENIED);
        assertThat(outcome(device,Role.ADMIN,AutonomousCapability.SYSTEM_MAINTENANCE)).isEqualTo(DENIED);
    }
    @Test
    void requesterWithoutRoleIsDenied(){
        AutonomousDevice device = agent(AutonomyLevel.AUTONOMOUS,false,AutonomousCapability.ALERT_READ);
        assertThat(outcome(device,null,AutonomousCapability.ALERT_READ)).isEqualTo(DENIED);
    }
    @Test
    void autonomyLevelGovernsMutations(){
        assertThat(outcome(agent(AutonomyLevel.OBSERVER,
                false,
                AutonomousCapability.ALERT_READ,
                AutonomousCapability.ALERT_ACKNOWLEDGE),
                Role.ADMIN,
                AutonomousCapability.ALERT_ACKNOWLEDGE)).isEqualTo(DENIED);

        assertThat(outcome(agent(AutonomyLevel.OBSERVER,
                false,
                AutonomousCapability.ALERT_READ,
                AutonomousCapability.ALERT_ACKNOWLEDGE),
                Role.ADMIN,
                AutonomousCapability.ALERT_READ)).isEqualTo(ALLOWED);

        assertThat(outcome(agent(AutonomyLevel.ASSISTED,false,AutonomousCapability.ALERT_ACKNOWLEDGE),
                Role.ADMIN,AutonomousCapability.ALERT_ACKNOWLEDGE)).isEqualTo(REQUIRES_APPROVAL);
        assertThat(outcome(agent(AutonomyLevel.SUPERVISED,false,AutonomousCapability.ALERT_ACKNOWLEDGE),
                Role.ADMIN,AutonomousCapability.ALERT_ACKNOWLEDGE)).isEqualTo(REQUIRES_APPROVAL);
        assertThat(outcome(agent(AutonomyLevel.AUTONOMOUS,true,AutonomousCapability.ALERT_ACKNOWLEDGE),
                Role.ADMIN,AutonomousCapability.ALERT_ACKNOWLEDGE)).isEqualTo(REQUIRES_APPROVAL);
        assertThat(outcome(agent(AutonomyLevel.AUTONOMOUS,false,AutonomousCapability.ALERT_ACKNOWLEDGE),
                Role.ADMIN,AutonomousCapability.ALERT_ACKNOWLEDGE)).isEqualTo(ALLOWED);
    }
    @Test
    void automaticMaintenanceAndRecoveryNeedExplicitFlags(){
        AutonomousDevice device = agent(
                AutonomyLevel.AUTONOMOUS,
                false,
                AutonomousCapability.SYSTEM_MAINTENANCE,
                AutonomousCapability.DEVICE_RESTART);
        assertThat(outcome(device,Role.ADMIN,AutonomousCapability.SYSTEM_MAINTENANCE)).isEqualTo(REQUIRES_APPROVAL);
        assertThat(outcome(device,Role.ADMIN,AutonomousCapability.DEVICE_RESTART)).isEqualTo(REQUIRES_APPROVAL);
        device.changeAutomation(true,true);
        assertThat(outcome(device,Role.ADMIN,AutonomousCapability.SYSTEM_MAINTENANCE)).isEqualTo(ALLOWED);
        assertThat(outcome(device,Role.TECHNICIAN,AutonomousCapability.DEVICE_RESTART)).isEqualTo(ALLOWED);
        assertThat(outcome(device,Role.TECHNICIAN,AutonomousCapability.SYSTEM_MAINTENANCE)).isEqualTo(DENIED);
    }
    @Test
    void suspendedDisabledAgentIsDenied(){
        AutonomousDevice device = agent(AutonomyLevel.AUTONOMOUS,false,AutonomousCapability.ALERT_READ);
        device.suspend();
        assertThat(outcome(device,Role.ADMIN,AutonomousCapability.ALERT_READ)).isEqualTo(DENIED);
        device.activate();
        assertThat(outcome(device,Role.ADMIN,AutonomousCapability.ALERT_READ)).isEqualTo(ALLOWED);
        device.disable();
        assertThat(outcome(device,Role.ADMIN,AutonomousCapability.ALERT_READ)).isEqualTo(DENIED);
        assertThatThrownBy(device::activate).isInstanceOf(IllegalStateException.class);
    }
    @Test
    void heartbeatAndExecutionCounters(){
        AutonomousDevice device = agent(AutonomyLevel.OBSERVER,false,AutonomousCapability.ALERT_READ);
        device.heartbeat(TO.plusSeconds(10));
        device.heartbeat(TO.plusSeconds(5));
        assertThat(device.getLastHeartbeatAt()).isEqualTo(TO.plusSeconds(10));
        device.recordExecution(TO,true);
        device.recordExecution(TO,false);
        assertThat(device.getTotalExecutions()).isEqualTo(2);
        assertThat(device.getFailedExecutions()).isEqualTo(1);
    }
    @Test
    void policyMirrorsControllerAccessRules(){
        for(Role r : EnumSet.of(Role.ADMIN,Role.TECHNICIAN,Role.HOME_USER,Role.AI_USER)){
            assertThat(AgentPermissionPolicy.permits(r,AutonomousCapability.ALERT_READ)).as(r.name()).isTrue();
        }
        assertThat(AgentPermissionPolicy.permits(Role.HOME_USER,AutonomousCapability.ALERT_RESOLVE)).isTrue();
        assertThat(AgentPermissionPolicy.permits(Role.AI_USER,AutonomousCapability.ALERT_RESOLVE)).isFalse();

        assertThat(AgentPermissionPolicy.permits(Role.TECHNICIAN,AutonomousCapability.CAMERA_CONFIGURATION)).isTrue();
        assertThat(AgentPermissionPolicy.permits(Role.HOME_USER,AutonomousCapability.SENSOR_CALIBRATION)).isFalse();

        assertThat(AgentPermissionPolicy.capabilitiesOf(Role.DEVELOPER)).isEmpty();
        assertThat(AgentPermissionPolicy.capabilitiesOf(Role.DEVICE_USER)).isEmpty();
        assertThat(AgentPermissionPolicy.capabilitiesOf(Role.ADMIN)).containsAll(EnumSet.allOf(AutonomousCapability.class));
    }
}
