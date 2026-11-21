package com.coelhotechne.detection_system.cluster;

import com.coelhotechne.detection_system.alert.config.StompSessionRegistry;
import com.coelhotechne.detection_system.alert.domain.enums.AlertCategory;
import com.coelhotechne.detection_system.alert.domain.enums.AlertStatus;
import com.coelhotechne.detection_system.alert.domain.enums.Severity;
import com.coelhotechne.detection_system.alert.event.AlertChangedEvent;
import com.coelhotechne.detection_system.alert.notification.AlertNotification;
import com.coelhotechne.detection_system.alert.notification.StompAlertChannel;
import com.coelhotechne.detection_system.cluster.bus.ClusterBusDispatcher;
import com.coelhotechne.detection_system.cluster.bus.ClusterMessage;
import com.coelhotechne.detection_system.cluster.config.ClusterProperties;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ClusterUnitTest {

    private static ClusterProperties node(String id){
        return new ClusterProperties(true,id, Duration.ofSeconds(10),
                Duration.ofSeconds(30),null,null);
    }
    @Test
    void defaultsAreSingleInstance(){
        ClusterProperties p = new ClusterProperties(null, null, null,
                null, null, null);

        assertThat(p.isEnabled()).isFalse();
        assertThat(p.nodeId()).matches("[A-Za-z0-9._-]{1,64}");
        assertThat(p.busTopic()).isEqualTo("detection-system/cluster/events");
    }
    @Test
    void  invalidConfigurationFailFast(){
        assertThatThrownBy(()-> new ClusterProperties(true, "no spaces",
                null, null, null, null)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(()-> new ClusterProperties(true, "a",
                Duration.ofSeconds(10), Duration.ofSeconds(15), null, null))
                .hasMessageContaining("node-timeout");
        assertThatThrownBy(()-> new ClusterProperties(true, "a",
                null, null, "cluster/#", null))
                .hasMessageContaining("wildcards");
    }
    @Test
    void dispatcherAppliesMessagesFromOtherNodesAndIgnoresItsOwnEcho()throws Exception{
        JsonMapper mapper = JsonMapper.builder().build();
        StompAlertChannel alertChannel = mock(StompAlertChannel.class);
        StompSessionRegistry registry = mock(StompSessionRegistry.class);
        ClusterBusDispatcher dispatcher = new ClusterBusDispatcher(mapper,node("node-a"),alertChannel,registry);

        AlertNotification alertNotification = new AlertNotification(UUID.randomUUID(),
                AlertChangedEvent.Change.CREATED,
                AlertStatus.TRIGGERED,
                Severity.HIGH,
                AlertCategory.DETECTION,
                "Motion",
                null,null,1, Instant.now());

        UUID device = UUID.randomUUID();

        dispatcher.onMessage(mapper.writeValueAsBytes(
                new ClusterMessage(ClusterMessage.Type.ALERT_BROADCAST,"node-b",alertNotification,null,null)));

        dispatcher.onMessage(mapper.writeValueAsBytes(
                new ClusterMessage(ClusterMessage.Type.ALERT_DELIVER,"node-b",alertNotification, List.of(device),null)));

        dispatcher.onMessage(mapper.writeValueAsBytes(
                new ClusterMessage(ClusterMessage.Type.SESSIONS_REVOKED,"node-b",alertNotification,null,List.of("x@y"))));

         dispatcher.onMessage(mapper.writeValueAsBytes(
                new ClusterMessage(ClusterMessage.Type.ALERT_BROADCAST,"node-a",alertNotification,null,null)));

         dispatcher.onMessage("not json".getBytes());

         verify(alertChannel,times(1)).broadcast(any());
         verify(alertChannel).deliverToDevices(any(),eq(List.of(device)));
         verify(registry).closeSessionsOf(List.of("x@y"));
    }
}
