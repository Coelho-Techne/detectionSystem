package com.coelhotechne.detection_system.detection.application;

import com.coelhotechne.detection_system.detection.domain.CamDetection;
import com.coelhotechne.detection_system.detection.domain.Detection;
import com.coelhotechne.detection_system.detection.domain.SensorDetection;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

final class DetectionSpecifications {

    private DetectionSpecifications() {
    }

    static Specification<Detection> of(DetectionFilter filter){
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filter.zoneId()!=null){
            predicates.add(criteriaBuilder.equal(root.get("zone").get("uuid"),filter.zoneId()));
        }
            if (filter.deviceType()!=null){
                predicates.add(criteriaBuilder.equal(root.get("deviceType"),filter.deviceType()));
            }
            if (filter.from()!=null){
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("detectedAt"),filter.from()));
            }
            if (filter.to()!=null){
                predicates.add(criteriaBuilder.lessThan(root.get("detectedAt"),filter.to()));
            }
            if (filter.sourceId()!=null){
                UUID sourceId = filter.sourceId();
                Subquery<UUID>bySensor = query.subquery(UUID.class);
                Root<SensorDetection>s = bySensor.from(SensorDetection.class);
                bySensor.select(s.get("uuid")).where(criteriaBuilder.equal(s.get("sensor").get("uuid"),sourceId));

                Subquery<UUID>byCam = query.subquery(UUID.class);
                Root<CamDetection> c = byCam.from(CamDetection.class);
                byCam.select(c.get("uuid")).where(criteriaBuilder.equal(c.get("cam").get("uuid"),sourceId));

                predicates.add(criteriaBuilder.or(root.get("uuid").in(bySensor),root.get("uuid").in(byCam)));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
