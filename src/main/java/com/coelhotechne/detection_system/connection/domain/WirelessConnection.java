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
public class WirelessConnection extends Connection{
    private final Integer signalStrengthDbm;
    private final Object clientHandle;

    public WirelessConnection(ApplicationProtocol protocol,
                              LinkType linkType, Integer signalStrengthDbm, Object clientHandle) {
        super(protocol, linkType);
        this.signalStrengthDbm = signalStrengthDbm;
        this.clientHandle = clientHandle;
    }

    @Override
    public boolean isReachable() {
        return clientHandle != null && (signalStrengthDbm == null || signalStrengthDbm > -90);
    }
}
