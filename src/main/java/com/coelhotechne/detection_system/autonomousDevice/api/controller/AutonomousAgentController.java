package com.coelhotechne.detection_system.autonomousDevice.api.controller;

import com.coelhotechne.detection_system.autonomousDevice.api.dto.*;
import com.coelhotechne.detection_system.autonomousDevice.application.AgentGuard;
import com.coelhotechne.detection_system.autonomousDevice.application.AutonomousAgentService;
import com.coelhotechne.detection_system.autonomousDevice.domain.AgentDecision;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/agent",produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Tag(name = "AI agents")
public class AutonomousAgentController {

    private final AutonomousAgentService service;

    @PreAuthorize(AgentAccess.ADMIN)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AgentCreatedResponse>register(@Valid @RequestBody AgentRegisterRequest request){
        AgentCreatedResponse created = service.register(request);
        return ResponseEntity.created(URI.create("/api/v1/agent/"+created.agentResponse().uuid())).body(created);
    }
    public ResponseEntity<AgentResponse>get(@PathVariable UUID id){
        return ResponseEntity.ok(service.get(id));
    }

    @PreAuthorize(AgentAccess.ADMIN)
    @PutMapping(value = "/{id}/status",consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AgentResponse>changeStatus(@PathVariable UUID id,
                                                     @Valid @RequestBody AgentStatusRequest request){
        return ResponseEntity.ok(service.changeStatus(id,request.status()));
    }

    @PreAuthorize(AgentAccess.REQUESTER)
    @PostMapping(value = "/{id}/decisions",consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<AgentDecisionResponse>decide(@PathVariable UUID id,
                                                       @RequestHeader(AgentGuard.AGENT_KEY_HEADER)
                                                       String agentKey,
                                                       @Valid @RequestBody
                                                       AgentDecisionRequest request,
                                                       Authentication authentication){
        AgentDecision decision = service.authorize(id,agentKey,authentication,request.capability(),request.target());
        return ResponseEntity.ok(AgentDecisionResponse.of(id,decision));
    }

    @PostMapping("/{id}/heartbeat")
    public ResponseEntity<AgentResponse>heartbeat(@PathVariable UUID id,
                                                  @RequestHeader(AgentGuard.AGENT_KEY_HEADER)
                                                  String agentKey){
        return ResponseEntity.ok(service.heartbeat(id,agentKey));
    }
    @PreAuthorize(AgentAccess.AUDIT)
    @GetMapping("/{id}/actions")
    public ResponseEntity<Page<AgentActionResponse>>actions(
            @PathVariable
            UUID id,
            @PageableDefault(size = 50,sort = "occurredAt",direction = Sort.Direction.DESC)
            Pageable pageable
    ){
        return ResponseEntity.ok(service.actions(id,pageable));
    }

}
