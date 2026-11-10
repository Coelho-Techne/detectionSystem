package com.coelhotechne.detection_system.sensor.domain.homologation;

import com.coelhotechne.detection_system.sensor.domain.homologation.enums.SensorHomologationStatus;
import com.coelhotechne.detection_system.sensor.domain.homologation.enums.SensorSelfTestFailure;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SensorHomologationRecordTest {
    private static final Instant TO = Instant.parse("2026-11-08T21:20:00Z");

    @Test
    void happyPathToApproval(){
        SensorHomologationRecord record = new SensorHomologationRecord();
        UUID req = UUID.randomUUID();
        record.startTest(req,TO);
        assertThat(record.getStatus()).isEqualTo(SensorHomologationStatus.AWAITING_RESPONSE);
        assertThat(record.applyTestResult(req,SensorSelfTestOutcome.passed("ok"),
                TO.plusSeconds(5))).isTrue();
        assertThat(record.getStatus()).isEqualTo(SensorHomologationStatus.PENDING_APPROVAL);
        record.approve("admin",TO.plusSeconds(10));
        assertThat(record.getStatus()).isEqualTo(SensorHomologationStatus.APPROVED);
    }
    @Test
    void responseForAnotherRequestIsIgnored(){
        SensorHomologationRecord r = new SensorHomologationRecord();
        r.startTest(UUID.randomUUID(),TO);
        assertThat(r.applyTestResult(UUID.randomUUID(),SensorSelfTestOutcome.passed("ok"),TO)).isFalse();
        assertThat(r.getStatus()).isEqualTo(SensorHomologationStatus.AWAITING_RESPONSE);
    }
    @Test
    void timeoutFailsTheTestAndAllowsRetry(){
        SensorHomologationRecord r = new SensorHomologationRecord();
        r.startTest(UUID.randomUUID(),TO);
        assertThat(r.expireIfOverdue(TO.plusSeconds(61),Duration.ofSeconds(60))).isTrue();
        assertThat(r.getStatus()).isEqualTo(SensorHomologationStatus.TEST_FAILED);
        assertThat(r.getTestFailure()).isEqualTo(SensorSelfTestFailure.TIMEOUT);
        r.startTest(UUID.randomUUID(),TO.plusSeconds(70));
        assertThat(r.getStatus()).isEqualTo(SensorHomologationStatus.AWAITING_RESPONSE);
    }
    @Test
    void cancelUnlockAStuckTest(){
        SensorHomologationRecord r = new SensorHomologationRecord();
        UUID req = UUID.randomUUID();
        r.startTest(req,TO);
        assertThat(r.cancelPendingTest()).isTrue();
        assertThat(r.getStatus()).isEqualTo(SensorHomologationStatus.PENDING_TEST);

        assertThat(r.applyTestResult(req,SensorSelfTestOutcome.passed("late"),TO)).isFalse();
        assertThat(r.cancelPendingTest()).isFalse();
    }
    @Test
    void decidedSensorNeedsReopenToTestAgain(){
        SensorHomologationRecord record = new SensorHomologationRecord();
        UUID req =UUID.randomUUID();
        record.startTest(req,TO);
        record.applyTestResult(req,SensorSelfTestOutcome.passed("ok"),TO);
        record.reject("admin","cabo solto",TO);
        assertThatThrownBy(()-> record.startTest(UUID.randomUUID(),TO)).isInstanceOf(IllegalStateException.class);
        record.reopen();
        assertThat(record.getStatus()).isEqualTo(SensorHomologationStatus.PENDING_TEST);
        assertThat(record.getRejectionReason()).isNull();
        record.startTest(UUID.randomUUID(),TO);
    }
    @Test
    void reopenOnlyFromDecidedStates(){
        SensorHomologationRecord r=new SensorHomologationRecord();
        assertThatThrownBy(r::reopen).isInstanceOf(IllegalStateException.class);
    }
}
