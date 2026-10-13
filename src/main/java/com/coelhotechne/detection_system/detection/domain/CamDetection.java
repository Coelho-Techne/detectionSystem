package com.coelhotechne.detection_system.detection.domain;

import com.coelhotechne.detection_system.cam.domain.base.BaseCam;
import com.coelhotechne.detection_system.cam.domain.homologation.enums.CamHomologationStatus;
import com.coelhotechne.detection_system.detection.domain.enums.DetectionDeviceType;
import com.coelhotechne.detection_system.detection.domain.enums.DetectionType;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "cam_detection",
        indexes = {
                @Index(name = "idx_cam_detection_cam", columnList = "cam_id"),
                @Index(name = "idx_cam_detection_track", columnList = "cam_id, track_id")
        })
public class CamDetection extends Detection{
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cam_id", nullable = false, updatable = false)
    private BaseCam cam;

    /** Id do rastro (ByteTrack). Um alerta por rastro, não por quadro. */
    @Column(name = "track_id", updatable = false)
    private Long trackId;

    @Column(name = "moving", updatable = false)
    private Boolean moving;

    /** Espécie/subclasse quando houver (ex.: {@code tityus_serrulatus}). */
    @Column(name = "taxon", updatable = false, length = 100)
    private String taxon;

    @Embedded
    private BoundingBox bbox;

    /** Referência ao recorte em disco local. Imagem não trafega no MQTT nem fica no banco. */
    @Column(name = "snapshot_ref", updatable = false, length = 255)
    private String snapshotRef;

    @Column(name = "model_version", updatable = false, length = 64)
    private String modelVersion;

    /** Exclusivo do JPA. */
    protected CamDetection() {
    }

    public CamDetection(UUID eventId,
                        BaseCam cam,
                        DetectionType type,
                        String unmappedType,
                        Instant detectedAt,
                        Instant receivedAt,
                        BigDecimal confidence,
                        Long trackId,
                        Boolean moving,
                        String taxon,
                        BoundingBox bbox,
                        String snapshotRef,
                        String modelVersion
                             ){
        super(eventId,
                DetectionDeviceType.CAMERA,
                type,
                unmappedType,
                Objects.requireNonNull(cam,"cam is required").getZone(),
                detectedAt,
                receivedAt,
                confidence,
                null,
                isApproved(cam));
        if (!DetectionTypeRules.allowsCamera(type)){
            throw new IllegalArgumentException(
                    "Detection type %s is not allowed for cameras"
                    .formatted(type));
        }
        this.cam=cam;
        this.trackId=trackId;
        this.moving=moving;
        this.taxon = taxon;
        this.bbox = bbox;
        this.snapshotRef=snapshotRef;
        this.modelVersion = modelVersion;


    }
    private static boolean isApproved(BaseCam cam){
        return cam.getHomologation() != null
                && cam.getHomologation().getStatus() == CamHomologationStatus.APPROVED;
    }
    public BaseCam getCam() {
        return cam;
    }

    @Override
    public UUID getSourceId() {
        return cam == null ? null : cam.getUuid();
    }

    public Long getTrackId() {
        return trackId;
    }

    public Boolean getMoving() {
        return moving;
    }

    public String getTaxon() {
        return taxon;
    }

    public BoundingBox getBbox() {
        return bbox;
    }

    public String getSnapshotRef() {
        return snapshotRef;
    }

    public String getModelVersion() {
        return modelVersion;
    }
}
