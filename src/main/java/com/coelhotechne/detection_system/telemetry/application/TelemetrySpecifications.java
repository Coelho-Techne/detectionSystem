package com.coelhotechne.detection_system.telemetry.application;


import com.coelhotechne.detection_system.telemetry.domain.Telemetry;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

final class TelemetrySpecifications {

    private TelemetrySpecifications(){

    }

    static Specification<Telemetry>of(TelemetryFilter filter){
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (filter.sensorId()!= null){
                predicates.add(criteriaBuilder.equal(root.get("sensor").get("uuid"),filter.sensorId()));
            }
            if (filter.zoneId()!=null){
                predicates.add(criteriaBuilder.equal(root.get("zone").get("uuid"),filter.zoneId()));
            }
            if (filter.metric() != null){
                predicates.add(criteriaBuilder.equal(root.get("metric"),filter.metric()));
            }
            if (filter.from()!= null){
                predicates.add(criteriaBuilder.equal(root.get("measuredAt"),filter.from()));
            }
            if (filter.to()!=null){
                predicates.add(criteriaBuilder.equal(root.get("measuredAt"),filter.to()));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }
}
