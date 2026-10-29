package com.coelhotechne.detection_system.connection.domain;

import com.coelhotechne.detection_system.connection.domain.enums.LinkType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
public class NetworkLink {
    @Enumerated(EnumType.STRING)
    @Column(name = "link_type")
    private LinkType linkType;
    @Column(name = "signal_strength_dbm")
    private Integer signalStrengthDbm; // null se WIRED_ETHERNET/WIRED_POE
    @Column(name = "link_ip_address")
    private String ipAddress;

    public NetworkLink(LinkType linkType, String ipAddress, Integer signalStrengthDbm){
        if (!linkType.isWireless() && signalStrengthDbm != null) {
            throw new IllegalArgumentException("Signal strength Dbm is only valid for wireless link types");
        }
        this.ipAddress=ipAddress;
        this.linkType=linkType;
        this.signalStrengthDbm=signalStrengthDbm;
    }

    public void updateSignal(int dbm){
        if (linkType==null||!linkType.isWireless()) {
            throw new IllegalStateException("This device is not wireless, don't have signal");
        }
        this.signalStrengthDbm=dbm;
    }
}
