package com.coelhotechne.detection_system.connection.domain;

import com.coelhotechne.detection_system.connection.domain.enums.ApplicationProtocol;
import com.coelhotechne.detection_system.connection.domain.enums.LinkType;

import java.time.Instant;

public abstract class Connection {

    protected final ApplicationProtocol protocol;
    protected final LinkType linkType;
    protected final Instant establishedAt;

    protected Connection(ApplicationProtocol protocol, LinkType linkType) {
        this.protocol = protocol;
        this.linkType = linkType;
        this.establishedAt = Instant.now();
    }

    public abstract boolean isReachable();
}