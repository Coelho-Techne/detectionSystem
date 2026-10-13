package com.coelhotechne.detection_system.detection.exceptions;

import com.coelhotechne.detection_system.detection.application.enums.DetectionOutcome;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import java.util.UUID;

@Getter
public class DetectionRejectedException extends ErrorResponseException {

    private final UUID sourceId;
    private final DetectionOutcome outcome;

    public DetectionRejectedException(UUID sourceId, DetectionOutcome outcome, String reason) {
        super(HttpStatus.UNPROCESSABLE_CONTENT, buildProblemDetail(sourceId, outcome, reason), null);
        this.sourceId = sourceId;
        this.outcome = outcome;
    }
    private static ProblemDetail buildProblemDetail(UUID sourceId, DetectionOutcome outcome, String reason) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, reason);
        pd.setTitle("Detection rejected");
        pd.setProperty("sourceId", String.valueOf(sourceId));
        pd.setProperty("result", outcome.tag());
        return pd;
    }
}
