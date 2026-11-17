package com.coelhotechne.detection_system.alert.notification;

import com.coelhotechne.detection_system.device.domain.Device;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class StompAlertChannel implements AlertChannel {

    private final ObjectProvider<SimpMessagingTemplate> messaging;

    public StompAlertChannel(ObjectProvider<SimpMessagingTemplate>messaging){
        this.messaging=messaging;
    }

    @Override
    public String name() {
        return "stomp";
    }

    @Override
    public void broadcast(AlertNotification notificaion) {
    messaging.ifAvailable(m -> m.convertAndSend("/topic/alerts",notificaion));
    }

    @Override
    public void deliver(AlertNotification notification, List<Device> recipients) {
    messaging.ifAvailable(m -> recipients.forEach(device ->
            m.convertAndSend("/topic/devices/"+device.getUuid()+"/alerts",notification)));
    }

    public void deliverToDevices(AlertNotification notification, List<UUID>deviceIds){
        messaging.ifAvailable(m->deviceIds.forEach(deviceId ->m.convertAndSend("/topic/devices/"+deviceId+"/alerts",notification)));
    }
}
