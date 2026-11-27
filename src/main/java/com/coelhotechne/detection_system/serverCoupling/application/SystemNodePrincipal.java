package com.coelhotechne.detection_system.serverCoupling.application;

import com.coelhotechne.detection_system.serverCoupling.domain.enums.SystemNodeRole;
import org.springframework.security.core.AuthenticatedPrincipal;

import java.util.UUID;

public record SystemNodePrincipal(
        UUID nodeId,
        String nodeIdentifier,
        SystemNodeRole rode
) implements AuthenticatedPrincipal {

    @Override
    public String getName() {
        return "NODE"+nodeIdentifier;
    }
}
