package com.coelhotechne.detection_system.connection.domain;

import com.coelhotechne.detection_system.connection.domain.enums.ApplicationProtocol;
import com.coelhotechne.detection_system.connection.domain.enums.LinkType;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class WiredConnection extends Connection{

    private final Object clientHandle; // referência ao client/sessão real (MQTT, RTSP...)

    public WiredConnection(ApplicationProtocol protocol, LinkType linkType, Object clientHandle) {
        super(protocol, linkType);
        this.clientHandle = clientHandle;
    }

    @Override
    public boolean isReachable() {
        return clientHandle != null; // troque por keepalive/ping específico do protocolo
    }
}
