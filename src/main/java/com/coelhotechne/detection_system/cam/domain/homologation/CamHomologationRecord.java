package com.coelhotechne.detection_system.cam.domain.homologation;

import com.coelhotechne.detection_system.cam.domain.homologation.enums.CamHomologationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

@Embeddable
@Getter
@NoArgsConstructor
public class CamHomologationRecord {
    private static final int RESON_MAX =500;
    private static final Set<CamHomologationStatus>TESTABLE = EnumSet.of(
            CamHomologationStatus.PENDING_TEST,
            CamHomologationStatus.TEST_FAILED,
            CamHomologationStatus.PENDING_APPROVAL
    );

    @Enumerated(EnumType.STRING)
    @Column(name = "homologation_status")
    private CamHomologationStatus status = CamHomologationStatus.PENDING_TEST;
    @Column(name = "homologation_last_tested_at")
    private Instant lastTestedAt;
    @Column(name = "homologation_failure_reason")
    private String failureReason;
    @Column(name = "homologation_resolved_codec")
    private String resolvedCodec;
    @Column(name = "homologation_decided_by")
    private String decidedBy;
    @Column(name = "homologation_decided_at")
    private Instant decidedAt;
    @Column(name = "homologation_rejection_reason")
    private String rejectionReason;

    public void requireTestable(){
        if (!TESTABLE.contains(status)){
            throw new IllegalStateException(
                    "Cannot teste while homologation status is "+status+"; reopen it first");
        }
    }

    public void applyTestResult(CamConnectionTestOutcome outcome,Instant now) {
        this.lastTestedAt = now;
        if (outcome.success()) {
            this.status = CamHomologationStatus.PENDING_APPROVAL;
            this.failureReason = null;
            this.resolvedCodec = outcome.resolvedCodec();
        } else {
            this.status = CamHomologationStatus.TEST_FAILED;
            this.failureReason = truncate(outcome.errorMessage());
            this.resolvedCodec = null;
        }
    }

    public void approve(String approvedBy,Instant now) {
        requireStatus(CamHomologationStatus.PENDING_APPROVAL, "approved");
        this.status = CamHomologationStatus.APPROVED;
        this.decidedBy = approvedBy;
        this.decidedAt = now;
        this.rejectionReason = null;
    }

    public void reject(String rejectedBy, String reason,Instant now) {
        requireStatus(CamHomologationStatus.PENDING_APPROVAL, "rejected");
        this.status = CamHomologationStatus.REJECTED;
        this.decidedBy = rejectedBy;
        this.decidedAt = now;
        this.rejectionReason = truncate(reason);
    }

    public void reopen(){
        if (status!=CamHomologationStatus.APPROVED && status!=CamHomologationStatus.REJECTED){
            throw new IllegalStateException("Cannot reopen while homologation status is: "+status);
        }
        clear();
    }

    public void resetForNewConnection(){
        clear();
    }

    private void clear(){
        this.status=CamHomologationStatus.PENDING_TEST;
        this.failureReason = null;
        this.resolvedCodec = null;
        this.decidedBy = null;
        this.decidedAt = null;
        this.rejectionReason = null;
    }

    private void requireStatus(CamHomologationStatus required, String action) {
        if (this.status != required) {
            throw new IllegalStateException(
                    "It is only possible" + action + " stems from  " + required + "; current status: " + this.status);
        }
    }

    private static String truncate(String value){
        return value == null|| value.length() <= RESON_MAX? value : value.substring(0,RESON_MAX);
    }
}

