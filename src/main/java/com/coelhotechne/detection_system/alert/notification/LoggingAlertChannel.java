package com.coelhotechne.detection_system.alert.notification;

import com.coelhotechne.detection_system.device.domain.Device;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Log4j2
public class LoggingAlertChannel implements AlertChannel{
    @Override
    public String name() {
        return "log";
    }

    @Override
    public void deliver(AlertNotification notification, List<Device> recipients) {
        log.info("Alert {} [{} {}] '{}' -> {} device(s): {}",
                notification.alertId(),
                notification.severity(),
                notification.changedEvent(),
                notification.title(),
                recipients.size(),
                recipients.stream().map(Device::getUuid).toList());
    }
}
