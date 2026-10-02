package com.coelhotechne.detection_system.sensor.domain.homologation;

import com.coelhotechne.detection_system.sensor.domain.homologation.enums.SensorHomologationStatus;
import com.coelhotechne.detection_system.sensor.domain.homologation.enums.SensorSelfTestFailure;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Embeddable
@Getter
@NoArgsConstructor
public class SensorHomologationRecord {
    private static final int REASON_MAX = 500;
    private static final int OUTPUT_MAX = 1000;
    @Enumerated(EnumType.STRING)
    @Column(name = "homologation_status", length = 30)
    private SensorHomologationStatus status = SensorHomologationStatus.PENDING_TEST;
    @Column(name = "homologation_request_id")
    private UUID pendingRequestId;
    @Column(name = "homologation_test_requested_at")
    private Instant testRequestedAt;
    @Column(name = "homologation_last_tested_at")
    private Instant lastTestedAt;
    @Enumerated(EnumType.STRING)
    @Column(name = "homologation_test_failure", length = 30)
    private SensorSelfTestFailure testFailure;
    @Column(name = "homologation_test_failure_detail", length = REASON_MAX)
    private String testFailureDetail;
    @Column(name = "homologation_decided_by")
    private String decidedBy;
    @Column(name = "homologation_decided_at")
    private Instant decidedAt;
    @Column(name = "homologation_test_output", length = OUTPUT_MAX)
    private String testOutput;
    @Column(name = "homologation_rejection_reason")
    private String rejectionReason;

    public void startTest(UUID requestId, Instant now){
        requireStatus(status==SensorHomologationStatus.PENDING_TEST
                ||status==SensorHomologationStatus.TEST_FAILED,"Starting test");
        status=SensorHomologationStatus.AWAITING_RESPONSE;
        pendingRequestId=requestId;
        testRequestedAt = now;
        testFailure = null;
        testFailureDetail = null;
        testOutput = null;
    }
    public boolean applyTestResult(UUID requestId, SensorSelfTestOutcome outcome,Instant now ){
        if (status!=SensorHomologationStatus.AWAITING_RESPONSE||requestId == null|| !requestId.equals(pendingRequestId)){
            return false;
        }
        lastTestedAt=now;
        pendingRequestId=null;
        if (outcome.success()){
            status=SensorHomologationStatus.PENDING_APPROVAL;
            testOutput = truncate(outcome.detail(),OUTPUT_MAX);
        }else {
            status=SensorHomologationStatus.TEST_FAILED;
            testFailure=outcome.failure();
            testOutput=truncate(outcome.detail(),REASON_MAX);
        }
        return true;
    }

    public boolean expireIfOverdue(Instant now, Duration timeout){
        if (status!=SensorHomologationStatus.AWAITING_RESPONSE ||
                testRequestedAt == null
                || testRequestedAt.plus(timeout).isAfter(now)){
            return false;
        }
        return applyTestResult(pendingRequestId,SensorSelfTestOutcome
                .failed(SensorSelfTestFailure
                        .TIMEOUT,"No response in: "+timeout
                        .toSeconds()+" s"),now);
    }
    public void approve(String by,Instant now){
        requireStatus(status==SensorHomologationStatus.PENDING_APPROVAL,"Approve");
        status=SensorHomologationStatus.APPROVED;
        decidedBy = by;
        decidedAt = now;
        rejectionReason = null;
    }
    public void reject(String by,String reason,Instant now){
        requireStatus(status==SensorHomologationStatus.PENDING_APPROVAL,"Reject");
        status=SensorHomologationStatus.REJECTED;
        decidedBy=by;
        decidedAt=now;
        rejectionReason=truncate(reason,REASON_MAX);
    }

    private void requireStatus(boolean allowed, String action) {
        if (!allowed) {
            throw new IllegalStateException("It's not possible " + action + " with homologation in " + status);
        }
    }
    private static String truncate(String value, int max) {
        return value == null || value.length() <= max ? value : value.substring(0, max);
    }


}
