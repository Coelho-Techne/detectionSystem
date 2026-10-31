package com.coelhotechne.detection_system.cam.domain.base;

import com.coelhotechne.detection_system.batterysupply.domain.PowerSupply;
import com.coelhotechne.detection_system.cam.domain.base.enums.CamStatus;
import com.coelhotechne.detection_system.cam.domain.base.enums.ImageQuality;
import com.coelhotechne.detection_system.cam.domain.connectioncam.CamConnectionParams;
import com.coelhotechne.detection_system.cam.domain.connectioncam.CamConnectionProfile;
import com.coelhotechne.detection_system.cam.domain.connectioncam.enums.CamProtocol;
import com.coelhotechne.detection_system.cam.domain.homologation.CamHomologationRecord;
import com.coelhotechne.detection_system.globalClass.entities.BaseEntity;
import com.coelhotechne.detection_system.installation.domain.Installation;
import com.coelhotechne.detection_system.zone.domain.Zone;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.time.Instant;
import java.util.Objects;

@Setter
@Getter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "base_cam")
@EqualsAndHashCode(callSuper = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Inheritance(strategy = InheritanceType.JOINED)
@EntityListeners(AuditingEntityListener.class)
public class BaseCam extends BaseEntity {
    @Column(name = "cam_model",nullable = false)
    private String camModel;
    @Column(name = "firmware_version", nullable = false)
    private String firmwareVersion;
    @Column(name = "serial_number", nullable = false, unique = true)
    private String serialNumber;
    @Column(name = "mac_address", nullable = false, unique = true)
    private String macAddress;
    @Column(name = "ip_address", nullable = false)
    private String ipAddress;
    @Column
    private String rtsp;
    @Enumerated(EnumType.STRING)
    private CamStatus camStatus;
    //resolution
    @Setter(AccessLevel.NONE)
    private Integer width;
    @Setter(AccessLevel.NONE)
    private Integer height;
    @Setter(AccessLevel.NONE)
    @Enumerated(EnumType.STRING)
    private ImageQuality imageQuality;
    @Column()
    private Float fps;
    @Column()
    private Integer bitrate;
    @Column
    private String compression;
    @Column(name = "frame_url")
    private String frameUrl;

    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",timezone = "UTC")
    @Column(name = "frame_timestamp")
    private Instant frameTimestamp;
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'",timezone = "UTC")
    @Column(name = "cam_timestamp")
    private Instant timestamp;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "type",   column = @Column(name = "power_type", length = 20)),
            @AttributeOverride(name = "status", column = @Column(name = "power_status", length = 20))
    })
    @EqualsAndHashCode.Exclude
    private PowerSupply powerSupply;
    @Embedded
    @EqualsAndHashCode.Exclude
    private Installation installation;
    @ManyToOne(fetch = FetchType.LAZY,optional = true)
    @JoinColumn(name = "zone_id")
    @EqualsAndHashCode.Exclude
    private Zone zone;
    @Embedded
    @EqualsAndHashCode.Exclude
    private CamConnectionProfile camConnection;

    @JsonIgnore
    @Setter(AccessLevel.NONE)
    @Column(name = "access_key", unique = true)
    private String accessKey;

    @Setter(AccessLevel.NONE)
    @Column(name = "last_communication")
    private Instant lastCommunication;

    @Setter(AccessLevel.NONE)
    @Embedded
    @EqualsAndHashCode.Exclude
    private CamHomologationRecord homologation = new CamHomologationRecord();

    //Automatico ::::::::::::::::::::::::::::::::::::::::::::::Selecoes

    public CamHomologationRecord getHomologation(){
        if (homologation == null){
            homologation=new CamHomologationRecord();
        }
        return homologation;
    }

    public void issueAccessKey(String accessKeyHash) {
        this.accessKey = Objects.requireNonNull(accessKeyHash,"accessKeyHash is required");
    }

    public void recordCommunication(Instant when) {
        this.lastCommunication = when;
    }

    public void setResolution(Integer width,Integer height){
        if (width==null||height==null){
            this.height=null;
            this.width=null;
            this.imageQuality=ImageQuality.UNKNOWN;
            return;
        }
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width e height need to be > 0");
        }
        this.width=width;
        this.height=height;
        this.imageQuality=calculateImageQuality(width,height);
    }

    private ImageQuality calculateImageQuality(Integer width,Integer height){
        if (width <= 640 && height <= 480) {
            return ImageQuality.LOW;
        }
        if (width <= 1280 && height <= 720) {
            return ImageQuality.LOW_MEDIUM;
        }
        if (width <= 1920 && height <= 1080) {
            return ImageQuality.MEDIUM;
        }
        if (width <= 7680 && height <= 4320) {
            return ImageQuality.HIGH;
        }
        return ImageQuality.UNKNOWN;
    }
    public void configureCamConnection(CamProtocol protocol, CamConnectionParams params) {
        switch (protocol) {
            case RTSP, NATIVE -> {
                if (params.streamUri() == null) {
                    throw new IllegalArgumentException("Stream Uri is required for the protocol: " + protocol);
                }
            }
            case ONVIF, PROPRIETARY_SDK -> {
                if (params.host() == null || params.port() == null) {
                    throw new IllegalArgumentException("host/port is required for the protocol: " + protocol);
                }
            }
        }
        this.camConnection = new CamConnectionProfile(
                protocol, params.streamUri(), params.host(), params.port(),
                params.vendorSdkId(), params.credentialRef(), params.capabilities(),
                params.linkType(), params.ipAddress(), params.signalStrengthDbm()
        );
        getHomologation().resetForNewConnection();
    }
}
