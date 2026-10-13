package com.coelhotechne.detection_system.detection.application;

import com.coelhotechne.detection_system.detection.domain.Detection;
import org.springframework.data.jpa.domain.Specification;

final class DetectionSpecifications {

    private DetectionSpecifications() {
    }

    static Specification<Detection> of(DetectionFilter filter){}
}
