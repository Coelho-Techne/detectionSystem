package com.coelhotechne.detection_system.alert.notification;

import com.coelhotechne.detection_system.device.domain.Device;

import java.util.List;

public interface AlertChannel {

    String name();

    default void broadCast(AlertNotification notificaion){}

    void deliver(AlertNotification notification, List<Device>recipients);
}
