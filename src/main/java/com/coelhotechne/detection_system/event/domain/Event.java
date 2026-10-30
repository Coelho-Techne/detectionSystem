package com.coelhotechne.detection_system.event.domain;

import com.coelhotechne.detection_system.alert.domain.enums.Severity;
import com.coelhotechne.detection_system.detection.domain.Detection;
import com.coelhotechne.detection_system.event.domain.enums.EventCategory;
import com.coelhotechne.detection_system.event.domain.enums.EventStatus;
import com.coelhotechne.detection_system.event.domain.enums.EventType;
import com.coelhotechne.detection_system.globalClass.entities.BaseEntity;
import com.coelhotechne.detection_system.zone.domain.Zone;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;


@Entity
@Data
@Table(name = "event")
@AllArgsConstructor
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Event extends BaseEntity {
    @ManyToOne
    @JoinColumn(name = "detection_id", nullable = false)
    private Detection detection;
    @ManyToOne
    @JoinColumn(name = "zone_id", nullable = false)
    private Zone zone;
    @Column(nullable = false)
    private String description;
    @Column(name = "event_time", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING,pattern = "dd/MM/yyyy HH:mm:ss")
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime eventTime;
    @Enumerated(EnumType.STRING)
    private EventType eventType;
    @Enumerated(EnumType.STRING)
    private EventStatus eventStatus;
    @Enumerated(EnumType.STRING)
    private EventCategory eventCategory;
    @Enumerated(EnumType.STRING)
    private Severity severity;
}