package com.coelhotechne.detection_system.autonomousDevice.application;

import com.coelhotechne.detection_system.autonomousDevice.api.dto.AgentActionResponse;
import com.coelhotechne.detection_system.autonomousDevice.api.dto.AgentCreatedResponse;
import com.coelhotechne.detection_system.autonomousDevice.api.dto.AgentRegisterRequest;
import com.coelhotechne.detection_system.autonomousDevice.api.dto.AgentResponse;
import com.coelhotechne.detection_system.autonomousDevice.domain.AgentActionRecord;
import com.coelhotechne.detection_system.autonomousDevice.domain.AgentDecision;
import com.coelhotechne.detection_system.autonomousDevice.domain.AutonomousDevice;
import com.coelhotechne.detection_system.autonomousDevice.domain.enums.AutonomousAgentStatus;
import com.coelhotechne.detection_system.autonomousDevice.domain.enums.AutonomousCapability;
import com.coelhotechne.detection_system.autonomousDevice.exceptions.AgentNotFoundException;
import com.coelhotechne.detection_system.autonomousDevice.exceptions.AgentUnauthorizedException;
import com.coelhotechne.detection_system.autonomousDevice.infraestructure.AgentActionRecordRepository;
import com.coelhotechne.detection_system.autonomousDevice.infraestructure.AutonomousDeviceRepository;
import com.coelhotechne.detection_system.globalClass.security.AccessKeyGenerator;
import com.coelhotechne.detection_system.globalClass.security.AccessKeyHasher;
import com.coelhotechne.detection_system.security.config.device.DeviceAuthenticationToken;
import com.coelhotechne.detection_system.security.domain.enums.Role;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Log4j2
public class AutonomousAgentService {
    private static final String ROLE_PREFIX = "ROLE_";

    private final AutonomousDeviceRepository agentRepository;
    private final AgentActionRecordRepository actionRepository;
    private final AccessKeyGenerator keyGenerator;
    private final AccessKeyHasher hasher;
    private final Clock clock;

    public AgentCreatedResponse register(AgentRegisterRequest request){
        String name = request.agentName().trim();
        if (agentRepository.existsByAgentName(name)){
            throw new ResponseStatusException(HttpStatus.CONFLICT,"agent_name already in use");
        }
        String rawKey = keyGenerator.generate();
        AutonomousDevice agent = AutonomousDevice.register(name,
                request.agentType(),
                request.autonomyLevel(),
                request.capability(),
                hasher.hash(rawKey),
                clock.instant());
        agent.describeModel(request.modelName(),request.modelVersion(),request.firmwareVersion());
        agent.changeAutonomy(request.autonomyLevel(),request.requireApproval() == null || request.requireApproval());
        agent.changeAutomation(Boolean.TRUE.equals(request.allowAutomaticMaintenance()),Boolean.TRUE.equals(request.allowAutomaticRecovery()));
        agent.changeExecutionLimits(request.maxConcurrentTasks() == null ? agent.getMaxConcurrentTasks(): request.maxConcurrentTasks(),
                request.maxRetries() == null ? agent.getMaxRetries():request.maxRetries(),
                request.executionTimeoutSeconds() == null ? agent.getExecutionTimeoutSeconds():request.executionTimeoutSeconds());
        AutonomousDevice saved = agentRepository.save(agent);
        return new AgentCreatedResponse(AgentResponse.from(saved),rawKey);
    }
    @Transactional(readOnly = true)
    public AgentResponse get(UUID agentId){
        return AgentResponse.from(requireAgent(agentId));
    }
    @Transactional
    public AgentResponse changeStatus(UUID agentId, AutonomousAgentStatus status){
        AutonomousDevice agent = requireAgent(agentId);
        switch (status){
            case ACTIVE -> agent.activate();
            case SUSPENDED -> agent.suspend();
            case DISABLED -> agent.disable();
            default -> throw new IllegalArgumentException("status must be ACTIVE, SUSPENDED  or DISABLED");
        }
        return AgentResponse.from(agentRepository.saveAndFlush(agent));
    }
    @Transactional(readOnly = true)
    public Page<AgentActionResponse>actions(UUID agentId, Pageable pageable){
        requireAgent(agentId);
        return actionRepository.findByAgentUuid(agentId,pageable).map(AgentActionResponse::from);
    }
    @Transactional
    public AgentResponse heartbeat(UUID agentId,String rawKey){
        AutonomousDevice agent = authenticate(agentId,rawKey);
        agent.heartBeat(clock.instant());
        return AgentResponse.from(agent);
    }
    @Transactional
    public AgentDecision authorize(UUID agentId, String rawKey, Authentication requester,
                                   AutonomousCapability capability, String target){
        AutonomousDevice agent = authenticate(agentId,rawKey);
        Role role = requesterRole(requester);
        AgentDecision decision = agent.decide(role,capability);
        String who = requester == null || requester.getName() == null ? "anonymous" : requester.getName();
        actionRepository.save(AgentActionRecord.of(
                agent.getUuid(),
                who,
                decision,
                target,
                clock.instant()
        ));
        if (!decision.isAllowed()){
            log.info("Agent {} {} {} for ({}): {}",agent.getAgentName(),
                    decision.capability(),
                    capability,
                    who,
                    role,
                    decision.reason());
        }
        return decision;
    }

    //

    private AutonomousDevice authenticate(UUID agentId,String rawKey){
        AutonomousDevice agent = agentRepository.findById(agentId).orElse(null);
        if (agent == null || hasher.matches(rawKey,agent.getAccessKey())){
            throw new AgentUnauthorizedException(null,agentId,"invalid agent credentials");
        }
        if (agent.getAgentStatus() == AutonomousAgentStatus.DISABLED){
            throw new AgentUnauthorizedException(agent.getAgentName(),agentId,"agent is disabled");
        }
        return agent;
    }
    private final AutonomousDevice requireAgent(UUID agentId){
        return agentRepository.findById(agentId).orElseThrow(()-> new AgentNotFoundException(agentId));
    }
    static Role requesterRole(Authentication authentication){
        if (authentication == null || !authentication.isAuthenticated()||authentication instanceof DeviceAuthenticationToken){
            return null;
        }
        for(GrantedAuthority authority : authentication.getAuthorities()){
            String value = authority.getAuthority();
            if (value != null && value.startsWith(ROLE_PREFIX)){
                try {
                    return Role.valueOf(value.substring(ROLE_PREFIX.length()));
                }catch (IllegalArgumentException ex){

                }
            }
        }
        return null;
    }

}
