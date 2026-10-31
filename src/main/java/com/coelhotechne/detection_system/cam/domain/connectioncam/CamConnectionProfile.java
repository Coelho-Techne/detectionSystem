package com.coelhotechne.detection_system.cam.domain.connectioncam;

import com.coelhotechne.detection_system.cam.domain.connectioncam.enums.CamCapability;
import com.coelhotechne.detection_system.cam.domain.connectioncam.enums.CamProtocol;
import com.coelhotechne.detection_system.connection.domain.NetworkLink;
import com.coelhotechne.detection_system.connection.domain.enums.LinkType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Embeddable
@Getter
@NoArgsConstructor
public class CamConnectionProfile {

    @Embedded
    private NetworkLink networkLink;
    @Enumerated(EnumType.STRING)
    @Column(name = "conn_protocol")
    private CamProtocol protocol;
    @Column(name = "conn_stream_uri")
    private String streamUri;       // RTSP / NATIVE
    @Column(name = "conn_host")
    private String host;           // ONVIF / PROPRIETARY_SDK
    @Column(name = "conn_port")
    private Integer port;
    @Column(name = "conn_vendor_sdk_id")
    private String vendorSdkId;     // PROPRIETARY_SDK — qual fabricante
    @Column(name = "conn_credential_ref")
    private String credentialRef;   // referência a um cofre, NUNCA a senha crua

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "cam_capabilities", joinColumns = @JoinColumn(name = "cam_uuid"))
    private Set<CamCapability> capabilities = new HashSet<>();

    // pacote-privado: só BaseCam.configureConnection() deve montar isso
    public CamConnectionProfile(CamProtocol protocol, String streamUri, String host, Integer port,
                                String vendorSdkId, String credentialRef, Set<CamCapability> capabilities,
                                LinkType linkType, String ipAddress, Integer signalStrengthDbm) {
        this.protocol = protocol;
        this.streamUri = streamUri;
        this.host = host;
        this.port = port;
        this.vendorSdkId = vendorSdkId;
        this.credentialRef = credentialRef;
        this.capabilities = capabilities != null ? capabilities : new HashSet<>();
        this.networkLink = linkType==null && ipAddress == null && signalStrengthDbm ==null
                ? null : new NetworkLink(linkType, ipAddress, signalStrengthDbm);
    }
}