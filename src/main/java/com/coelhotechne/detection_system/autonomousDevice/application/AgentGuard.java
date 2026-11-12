package com.coelhotechne.detection_system.autonomousDevice.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("agentGuard")
@RequiredArgsConstructor
public class AgentGuard {
    public static final String AGENT_ID_HEADER = "X-Agent-Id";
    public static final String AGENT_KEY_HEADER = "X-Agent-Key";

    private final AutonomousAgentService service;
}
