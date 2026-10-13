package com.coelhotechne.detection_system.detection.application;

import com.coelhotechne.detection_system.detection.application.enums.DetectionOutcome;
import com.coelhotechne.detection_system.detection.domain.Detection;

public record DetectionRecordResult(
        DetectionOutcome outcome,
        Detection detection,
        String reason
){
    public static DetectionRecordResult stored(Detection detection){
        return new DetectionRecordResult(DetectionOutcome.STORED,detection,null);
    }
    public static DetectionRecordResult duplicate(Detection existing){
        return new DetectionRecordResult(DetectionOutcome.DUPLICATE,existing,null);
    }
    public static DetectionRecordResult discarded(DetectionOutcome outcome, String reason){
        return new DetectionRecordResult(outcome,null,reason);
    }
}
