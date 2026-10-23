package com.coelhotechne.detection_system.alert.application;

import com.coelhotechne.detection_system.alert.domain.Alert;
import com.coelhotechne.detection_system.alert.domain.enums.AlertStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

final class AlertSpecifications {
    private AlertSpecifications(){

    }

    static Specification<Alert>of(AlertFilter filter){
        return ((root, query, criteriaBuilder) -> {
            List<Predicate>predicates=new ArrayList<>();
            if (filter.status()!=null){
                predicates.add(criteriaBuilder.equal(root.get("status"),filter.status()));
            }else {
                predicates.add(root.get("status").in(AlertStatus.OPEN));
            }
            if (filter.severity()!=null){
                predicates.add(criteriaBuilder.equal(root.get("severity"),filter.severity()));
            }
            if (filter.category()!=null){
                predicates.add(criteriaBuilder.equal(root.get("category"),filter.category()));
            }if (filter.zoneId()!=null){
                predicates.add(criteriaBuilder.equal(root.get("zone").get("uuid"),filter.zoneId()));
            }if (filter.sourceId()!=null){
                predicates.add(criteriaBuilder.equal(root.get("sourceId"),filter.sourceId()));
            }if (filter.from()!=null){
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("lastOccurredAt"),filter.from()));
            }if (filter.to()!=null) {
                predicates.add(criteriaBuilder.lessThan(root.get("lastOccurredAt"), filter.to()));
            }
            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        });
    }
}
