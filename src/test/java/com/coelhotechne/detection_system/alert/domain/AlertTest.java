package com.coelhotechne.detection_system.alert.domain;

import com.coelhotechne.detection_system.alert.domain.enums.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class AlertTest {

    private static final Instant TO = Instant.parse("2026-10-26T17:04:00Z");

    private static Alert alert(Severity severity){
        UUID source = UUID.randomUUID();
        return new Alert(
                new AlertDecision(AlertCategory.DETECTION, AlertType.DETECTED,severity,
                        AlertSourceType.SENSOR,source,null,null,"Smoke detected",
                        null,AlertDecision.key(AlertSourceType.SENSOR,source,AlertType.DETECTED,
                        "SMOKE"),TO),null,null);
    }

    @Test
    void acknowledgeThenResolve(){
        Alert a = alert(Severity.HIGH);
        a.acknowledge("ana",TO.plusSeconds(10));
        a.resolve("ana","verificado",TO.plusSeconds(60));

        assertThat(a.getStatus()).isEqualTo(AlertStatus.RESOLVED);
        assertThat(a.getAcknowledgedBy()).isEqualTo("ana");
        assertThat(a.getClosedAt()).isEqualTo(TO.plusSeconds(60));
    }
    @Test
    void acknowledgeTwiceIsRejected(){
        Alert a = alert(Severity.HIGH);
        a.acknowledge("ana",TO);
        assertThatThrownBy(()-> a.acknowledge("bia",TO)).isInstanceOf(IllegalStateException.class);
    }
    @Test
    void closedIsFinal(){
        Alert a = alert(Severity.HIGH);
        a.acknowledge("ana",TO);
        a.clear(TO);
        assertThatThrownBy(()-> a.resolve("ana","x",TO)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(()-> a.acknowledge("ana",TO)).isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(()-> a.clear(TO)).isInstanceOf(IllegalStateException.class);
    }
    @Test
    void criticalNeedsNote(){
        Alert a = alert(Severity.CRITICAL);
        assertThatThrownBy(()-> a.resolve("ana","  ",TO)).isInstanceOf(IllegalStateException.class);
        a.resolve("ana","Smoke from kitchen",TO);
        assertThat(a.getStatus()).isEqualTo(AlertStatus.RESOLVED);
    }
    @Test
    void equalityIsNotByFields(){
        Alert a = alert(Severity.LOW);
        int before = a.hashCode();
        a.acknowledge("ana",TO);
        assertThat(a.hashCode()).isEqualTo(before);
    }
}
