package com.coelhotechne.detection_system.connection.application;

import com.coelhotechne.detection_system.connection.domain.Connection;
import com.coelhotechne.detection_system.connection.domain.ConnectionParams;
import com.coelhotechne.detection_system.connection.domain.enums.ApplicationProtocol;
import com.coelhotechne.detection_system.connection.exceptions.ConnectionNotFoundException;
import com.coelhotechne.detection_system.connection.exceptions.enums.ConnectionError;
import com.coelhotechne.detection_system.connection.infrastructure.ConnectionResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FindConnection {

    private final List<ConnectionResolver> resolvers; // um resolver por ApplicationProtocol

    public Connection execute(ApplicationProtocol protocol, ConnectionParams params) {
        return resolvers.stream()
                .filter(r -> r.supports(protocol))
                .findFirst()
                .map(r -> r.resolve(params))
                .orElseThrow(() -> new ConnectionNotFoundException(
                        ConnectionError.UNSUPPORTED_PROTOCOL,
                        "Nenhum resolver disponível para " + protocol));
    }
}
