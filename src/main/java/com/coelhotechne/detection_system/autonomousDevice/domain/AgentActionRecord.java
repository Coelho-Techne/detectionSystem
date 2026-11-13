package com.coelhotechne.detection_system.autonomousDevice.domain;


import com.coelhotechne.detection_system.autonomousDevice.domain.enums.AgentDecisionOutcome;
import com.coelhotechne.detection_system.autonomousDevice.domain.enums.AutonomousCapability;
import com.coelhotechne.detection_system.globalClass.entities.BaseEntity;
import com.coelhotechne.detection_system.security.domain.enums.Role;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "autonomous_agent_action", indexes = {
        @Index(name = "idx_agent_action_agent_time",columnList = "agent_uuid, occurred_at"),
        @Index(name = "idx_agent_action_request",columnList = "requester, occurred_at")
})
public class AgentActionRecord extends BaseEntity {

    public static final int TARGET_MAX = 120;
    public static final int REASON_MAX = 255;

    @Column(name = "agent_id",nullable = false,updatable = false)
    private UUID agentId;
    @Column(name = "requester",nullable = false,updatable = false)
    private String requester;
    @Enumerated(EnumType.STRING)
    @Column(name = "requester_role",updatable = false,length = 20)
    private Role requesterRole;
    @Enumerated(EnumType.STRING)
    @Column(name = "capability",updatable = false,length = 40)
    private AutonomousCapability capability;
    @Column(name = "target",updatable = false,length = TARGET_MAX)
    private String target;
    @Enumerated(EnumType.STRING)
    @Column(name = "outcome",nullable = false,updatable = false,length = 20)
    private AgentDecisionOutcome outcome;
    @Column(name = "reason",nullable = false,updatable = false)
    private String reason;
    @Column(name = "occurred_at",nullable = false,updatable = false)
    private Instant occurredAt;

    public static AgentActionRecord of(UUID agentId,String requester,AgentDecision decision,
                                       String target,Instant occurredAt){
        AgentActionRecord r = new AgentActionRecord();
        r.agentId = Objects.requireNonNull(agentId,"agentId is required");
        r.requester = Objects.requireNonNull(requester,"requester is required");
        r.requesterRole = decision.requesterRole();
        r.capability= decision.capability();
        r.outcome = decision.outcome();
        r.reason = truncate(decision.reason(),REASON_MAX);
        r.target = truncate(target,TARGET_MAX);
        r.occurredAt = Objects.requireNonNull(occurredAt,"occurredAt is required");
        return r;
    }

    private static String truncate(String value, int max){
        return value == null || value.length() <= max ? value : value.substring(0,max);
    }
}
