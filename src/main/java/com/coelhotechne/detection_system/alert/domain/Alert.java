package com.coelhotechne.detection_system.alert.domain;


import com.coelhotechne.detection_system.alert.domain.enums.*;
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
    @Column(name = "severity",length = 10)
    private Severity severity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",nullable = false, length = 15)
    private AlertStatus status;
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
    private int occurrenceCount;

    @Column(name = "first_ocurred_at",nullable = false,updatable = false)
    private Instant firstOccurredAt;

    @Column(name = "last_occured_at",nullable = false)
    private Instant lastOccurredAt;

    @Column(name = "acknowledged_by",length = 255)
    private String acknowledgedBy;

    @Column(name = "ackonowledge_at")
    private Instant acknowledgedAt;

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
        this.category = decision.category();
        this.type = decision.type();
        this.severity = decision.severity();
        this.sourceType = decision.sourceType();
        this.sourceId = decision.sourceId();
        this.detection = detection;
        this.zone = zone;
        this.title = decision.title();
        this.description = decision.description();
        this.dedupKey = decision.dedupKey();
        this.firstOccurredAt = decision.occurredAt();
        this.lastOccurredAt = decision.occurredAt();
    }

    public void acknowledge(String user, Instant at){
        requireStatus(AlertStatus.TRIGGERED,"acknowledge");
        this.status = AlertStatus.ACKNOWLEDGED;
        this.acknowledgedBy = Objects.requireNonNull(user,"user is required");
        this.acknowledgedAt = Objects.requireNonNull(at,"at is required");
    }

    public void resolve(String user, String note,Instant at){
        requireOpen("resolve");
        if (severity == Severity.CRITICAL && (note == null|| note.isBlank())){
            throw new IllegalArgumentException("A resolution note is required for CRITICAL alerts");
        }
        this.status = AlertStatus.RESOLVED;
        this.resolvedBy = Objects.requireNonNull(user," user is required");
        this.resolutionNote = note == null || note.isBlank() ? null : truncate(note.trim());
        this.closedAt = Objects.requireNonNull(at," at is required");
    }

    public void clear(Instant at){
        requireOpen("clear");
        this.status = AlertStatus.CLEARED;
        this.closedAt = Objects.requireNonNull(at,"at is required");
    }

    private void requireOpen(String action){
        if (!status.isOpen()){
            throw new IllegalArgumentException("Cannot %s an alert in status %s"
                    .formatted(action,status));
        }
    }

    private void requireStatus(AlertStatus expected, String action){
        if (status!=expected){
            throw new IllegalArgumentException("Cannot %s an alert in status %s"
                    .formatted(action,status));
        }
    }

    private static String truncate(String value){
        return value.length() <= TEXT_MAX ? value : value.substring(0,TEXT_MAX);
    }

    // :::::::::::::::::::::::::::: getters ::::::::::::::::::::::::::::
    public AlertCategory getCategory() { return category; }
    public AlertType getType() { return type; }
    public Severity getSeverity() { return severity; }
    public AlertStatus getStatus() { return status; }
    public AlertSourceType getSourceType() { return sourceType; }
    public UUID getSourceId() { return sourceId; }
    public Detection getDetection() { return detection; }
    public Zone getZone() { return zone; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getDedupKey() { return dedupKey; }
    public int getOccurrenceCount() { return occurrenceCount; }
    public Instant getFirstOccurredAt() { return firstOccurredAt; }
    public Instant getLastOccurredAt() { return lastOccurredAt; }
    public String getAcknowledgedBy() { return acknowledgedBy; }
    public Instant getAcknowledgedAt() { return acknowledgedAt; }
    public String getResolvedBy() { return resolvedBy; }
    public String getResolutionNote() { return resolutionNote; }
    public Instant getClosedAt() { return closedAt; }
}
