package com.coelhotechne.detection_system.detection.domain;

import com.coelhotechne.detection_system.cam.domain.base.BaseCam;
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
public class CamFixedDetection extends Detection{
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

    /** Bounding box em pixels do quadro analisado: [x1, y1, x2, y2]. */
    @Column(name = "bbox_x1", updatable = false)
    private Integer bboxX1;
    @Column(name = "bbox_y1", updatable = false)
    private Integer bboxY1;
    @Column(name = "bbox_x2", updatable = false)
    private Integer bboxX2;
    @Column(name = "bbox_y2", updatable = false)
    private Integer bboxY2;

    /** Referência ao recorte em disco local. Imagem não trafega no MQTT nem fica no banco. */
    @Column(name = "snapshot_ref", updatable = false, length = 255)
    private String snapshotRef;

    @Column(name = "model_version", updatable = false, length = 64)
    private String modelVersion;

    /** Exclusivo do JPA. */
    protected CamFixedDetection() {
    }

    public CamFixedDetection(UUID eventId,
                             BaseCam cam,
                             DetectionType type,
                             String unmappedType,
                             Instant detectedAt,
                             Instant receivedAt,
                             BigDecimal confidence,
                             Long trackId,
                             Boolean moving,
                             String taxon,
                             int[] bbox,
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
                null);
        if (!DetectionTypeRules.allowsCamera(type)){
            throw new IllegalArgumentException(
                    "Detection type %s is not allowed for cameras"
                    .formatted(type));
        }
        this.cam=cam;
        this.trackId=trackId;
        this.moving=moving;
        this.taxon = taxon;
        this.snapshotRef=snapshotRef;
        this.modelVersion = modelVersion;
        if (bbox!=null){
            if (bbox.length!=4 ||bbox[0]>=bbox[2]||bbox[1]>=bbox[3]){
                throw new IllegalArgumentException("bbox must be [x1,y1,x2,y2] with x1 < x2 and y1 < y2");
            }
            this.bboxX1=bbox[0];
            this.bboxX2=bbox[1];
            this.bboxY1=bbox[2];
            this.bboxY2=bbox[3];
        }

    }

    public BaseCam getCam(){
        return cam;
    }
    public UUID getSourceId(){
        return cam == null ? null : cam.getUuid();
    }

    public Long getTrackId(){
        return trackId;
    }
    public Boolean getMoving(){
        return moving;
    }
    public String getTaxon(){
        return taxon;
    }

    public int[] getBox(){
        if (bboxX1 == null || bboxY1 == null || bboxX2 == null || bboxY2 == null){
            return null;
        }

        return new int[]{bboxX1,bboxY1,bboxX2,bboxY2};
    }

    public String getSnapshotRef(){return snapshotRef;}
    public String getModelVersion(){return modelVersion;}
}
