package com.coelhotechne.detection_system.alert.notification;


import com.coelhotechne.detection_system.cluster.bus.ClusterEventBus;
import com.coelhotechne.detection_system.device.domain.Device;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClusterAlertChannel implements AlertChannel {

    private final ClusterEventBus bus;

    public ClusterAlertChannel(ClusterEventBus bus){
        this.bus=bus;
    }
    @Override
    public String name() {
        return "cluster";
    }
    @Override
    public void broadcast(AlertNotification notification){
        if (bus.isActive()){
            bus.broadcastAlert(notification);
        }
    }

    @Override
    public void deliver(AlertNotification notification, List<Device> recipients) {
    if (bus.isActive()){
        bus.deliverAlert(notification,recipients.stream().map(Device::getUuid).toList());
    }
    }
}
