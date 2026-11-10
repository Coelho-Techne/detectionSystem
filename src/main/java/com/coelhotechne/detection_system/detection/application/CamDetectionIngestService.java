package com.coelhotechne.detection_system.detection.application;

import com.coelhotechne.detection_system.detection.api.dto.DetectionMapper;
import com.coelhotechne.detection_system.detection.api.dto.DetectionResponse;
import com.coelhotechne.detection_system.detection.application.enums.DetectionOutcome;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CamDetectionIngestService {
    private final DetectionRecorder recorder;
    private final DetectionMapper mapper;

    public record Outcome(DetectionOutcome outcome, DetectionResponse detection,String reason){
    }
    @Transactional
    public Outcome ingest(UUID camId,CamDetectionCommand command){
        DetectionRecordResult result = recorder.recordFromCamera(camId,command);
        DetectionResponse body = result.detection() == null ? null : mapper.toResponse(result.detection());
        return new Outcome(result.outcome(),body,result.reason());
    }
}
