package com.coelhotechne.detection_system.zone.domain;

import com.coelhotechne.detection_system.globalClass.entities.BaseEntity;
import com.coelhotechne.detection_system.zone.domain.enums.ZoneType;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(name = "zone")
@EntityListeners(AuditingEntityListener.class)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class Zone extends BaseEntity {
    @Column(nullable = false)
    private String name;
    @Column
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(name = "zone_type", nullable = false)
    private ZoneType zoneType;
    @Column(name = "center_latitude", precision = 9, scale = 6, nullable = false)
    private BigDecimal centerLatitude;
    @Column(name = "center_longitude", precision = 9, scale = 6, nullable = false)
    private BigDecimal centerLongitude;
    @Column(name = "radius_meters", nullable = false)
    private Double radiusMeters;
    @Column(nullable = false)
    private Boolean active = true;
    @Column(name = "created_zone_at_day")
    private LocalDate createdZoneAtDay;
}
