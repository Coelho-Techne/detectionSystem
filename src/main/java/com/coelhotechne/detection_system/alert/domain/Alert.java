package com.coelhotechne.detection_system.alert.domain;


import com.coelhotechne.detection_system.alert.domain.enums.AlertCategory;
import com.coelhotechne.detection_system.alert.domain.enums.AlertSourceType;
import com.coelhotechne.detection_system.alert.domain.enums.AlertType;
import com.coelhotechne.detection_system.detection.domain.Detection;
import com.coelhotechne.detection_system.globalClass.entities.BaseEntity;
import com.coelhotechne.detection_system.zone.domain.Zone;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "alert",
        indexes = {
                @Index(name = "idx_alert_status_severity",columnList = "status, severity, last_occurred_at"),
                @Index(name = "idx_alert_zone_occurred",columnList = "zone_id, first_occurred_at"),
                @Index(name = "idx_alert_source",columnList = "source_type, source_id")
        })
public class Alert extends BaseEntity {

    public static final int TITLE_MAX = 120;
    public static final int TEXT_MAX = 500;
    public static final int DEDUP_KEY_MAX = 150;

    @Enumerated(EnumType.STRING)
    @Column(name = "category",nullable = false,length = 20,updatable = false)
    private AlertCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "type",nullable = false,length = 30,updatable = false)
    private AlertType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type",nullable = false,length = 10,updatable = false)
    private AlertSourceType sourceType;

    @Column(name = "source_id",updatable = false)
    private UUID sourceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "detection_id",updatable = false)
    private Detection detection;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id",updatable = false)
    private Zone zone;

    @Column(name = "title",nullable = false,length = TITLE_MAX)
    private String title;

    @Column(name = "description",length = TITLE_MAX)
    private String description;

    @Column(name = "dedup_key",nullable = false,length = DEDUP_KEY_MAX,updatable = false)
    private String dedupKey;

    @Column(name = "open_dedup_key",length = DEDUP_KEY_MAX,insertable = false,updatable = false)
    private String openDedupKey;
    @Column(name = "ocurrence_count",nullable = false)
    private int ocurrenceCount;
    @Column(name = "first_ocurred_at",nullable = false,updatable = false)
    private Instant firstOcurredAt;
    @Column(name = "last_occured_at",nullable = false)
    private Instant lastOcurredAt;
    @Column(name = "acknowledged_by",length = 255)
    private String ackonowledgeBy;
    @Column(name = "ackonowledge_at")
    private Instant ackonowledgeAt;
    @Column(name = "resolved_by",length = 255)
    private String resolvedBy;
    @Column(name = "resolution_note",length = TEXT_MAX)
    private String resolutionNote;
    @Column(name = "closed_at")
    private Instant closedAt;

    protected Alert(){
    }


    public Alert(AlertDecision decision,Detection detection,Zone zone){
        Objects.requireNonNull(decision,"decision is required");
        this.category = decision.category();
        this.type = decision.type();
    }
}
