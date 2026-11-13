package com.coelhotechne.detection_system.autonomousDevice.application;

import com.coelhotechne.detection_system.autonomousDevice.domain.AgentDecision;
import com.coelhotechne.detection_system.autonomousDevice.domain.enums.AutonomousCapability;
import com.coelhotechne.detection_system.autonomousDevice.exceptions.AgentForbiddenException;
import com.coelhotechne.detection_system.autonomousDevice.exceptions.AgentUnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.UUID;

@Component("agentGuard")
@RequiredArgsConstructor
public class AgentGuard {
    public static final String AGENT_ID_HEADER = "X-Agent-Id";
    public static final String AGENT_KEY_HEADER = "X-Agent-Key";

    private final AutonomousAgentService service;

    public boolean check(String capability){
        HttpServletRequest request = currentRequest();
        if (request == null){
            return true;
        }
        String id = request.getHeader(AGENT_ID_HEADER);
        String key = request.getHeader(AGENT_KEY_HEADER);
        if (id == null && key == null){
            return true;
        }
        if (id == null || key == null){
            throw new AgentUnauthorizedException(null,null,AGENT_ID_HEADER+" and "+AGENT_ID_HEADER+" must be sent together");
        }

        UUID agentId = parse(id);
        AutonomousCapability cap = AutonomousCapability.valueOf(capability);
        Authentication requester = SecurityContextHolder.getContext().getAuthentication();
        AgentDecision decision = service.authorize(agentId,key, requester,cap, request.getMethod()+
                " " +request.getRequestURI());
        if (!decision.isAllowed()){
            throw new AgentForbiddenException(agentId,decision);
        }
        return true;
    }
    private static UUID parse(String id){
        try {
            return UUID.fromString(id.trim());
        }catch (IllegalArgumentException ex){
            throw new AgentUnauthorizedException(null,null,AGENT_ID_HEADER + "is not a valid UUID");
        }
    }
    private static HttpServletRequest currentRequest(){
        return RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes ? attributes.getRequest() : null;
    }
}
