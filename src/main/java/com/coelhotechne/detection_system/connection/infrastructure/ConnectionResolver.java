package com.coelhotechne.detection_system.connection.infrastructure;

import com.coelhotechne.detection_system.connection.domain.Connection;
import com.coelhotechne.detection_system.connection.domain.ConnectionParams;
import com.coelhotechne.detection_system.connection.domain.enums.ApplicationProtocol;

public interface ConnectionResolver {
    boolean supports(ApplicationProtocol protocol);
    Connection resolve(ConnectionParams params);
}
