package com.coelhotechne.detection_system.serverCoupling.event;

import java.util.UUID;

public record SystemNodeRemovedEvent(
        UUID nodeId,
        boolean deleted
) {
}
