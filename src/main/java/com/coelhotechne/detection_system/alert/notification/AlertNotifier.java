package com.coelhotechne.detection_system.alert.notification;

import com.coelhotechne.detection_system.alert.domain.Alert;
import com.coelhotechne.detection_system.alert.event.AlertChangedEvent;
import com.coelhotechne.detection_system.alert.infrastructure.AlertRepository;
import com.coelhotechne.detection_system.device.domain.Device;
import com.coelhotechne.detection_system.device.infrastructure.DeviceAlertSubscriptionRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@Log4j2
public class AlertNotifier {
    private final AlertRepository repository;
    private final DeviceAlertSubscriptionRepository subscriptionRepository;
    private final List<AlertChannel>channels;

    public AlertNotifier(
            AlertRepository alertRepository,
            DeviceAlertSubscriptionRepository alertSubscriptionRepository,
            List<AlertChannel>channels
    ){
        this.repository=alertRepository;
        this.subscriptionRepository=alertSubscriptionRepository;
        this.channels=channels;
    }
    @Async("alertNotificationExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW,readOnly = true)
    public void onAlertChanged(AlertChangedEvent event){
        Alert alert = repository.findById(event.alertId()).orElse(null);
        if (alert == null){
            return;
        }
        AlertNotification notification = toNotification(alert,event.change());
        for (AlertChannel channel : channels){
            try {
                channel.broadCast(notification);
            }catch (RuntimeException ex){
                log.warn("Channel {} failed to broadcast alert {}",channel.name(),alert.getUuid(),ex);
            }
        }

        if (event.change()!= AlertChangedEvent.Change.CREATED){
            return;
        }
        List<Device> recipients = subscriptionRepository.findRecipients(
                    event.zoneId(),
                    alert.getSeverity().andBelow());
        if (recipients.isEmpty()){
            log.debug("Alert {} has no subscribed device",alert.getUuid());
            return;
        }
        for (AlertChannel channel : channels){
            try {
                channel.deliver(notification,recipients);
            }catch (RuntimeException ex){
                log.warn("Channel {} failed to deliver alert {}",channel.name(),alert.getUuid(),ex);
            }
        }
    }


    private static AlertNotification toNotification(Alert alert,AlertChangedEvent.Change change){
        return new AlertNotification(
                alert.getUuid(),
                change,
                alert.getStatus(),
                alert.getSeverity(),
                alert.getCategory(),
                alert.getTitle(),
                alert.getZone() == null?null: alert.getZone().getUuid(),
                alert.getZone() == null?null: alert.getZone().getName(),
                alert.getOccurrenceCount(),
                alert.getLastOccurredAt()
        );
    }
}
