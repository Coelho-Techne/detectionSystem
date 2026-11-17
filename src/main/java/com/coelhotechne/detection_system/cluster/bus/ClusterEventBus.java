package com.coelhotechne.detection_system.cluster.bus;

import com.coelhotechne.detection_system.alert.notification.AlertNotification;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ClusterEventBus {
    boolean isActive();

    void publish(ClusterMessage message);

    String nodeId();

    default void broadcastAlert(AlertNotification notification){
        publish(new ClusterMessage(ClusterMessage.Type.ALERT_BROADCAST,nodeId(),notification,null,null));
    }

    default void deliverAlert(AlertNotification notification, List<UUID>deviceIds){
        publish(new ClusterMessage(ClusterMessage.Type.ALERT_DELIVER,nodeId(),notification,deviceIds,null));
    }

    default void revokeSessions(Collection<String> principals){
        publish(new ClusterMessage(ClusterMessage.Type.SESSIONS_REVOKED,nodeId(),null,null,List.copyOf(principals)));
    }
}
