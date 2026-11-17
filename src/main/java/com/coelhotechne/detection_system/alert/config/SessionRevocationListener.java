package com.coelhotechne.detection_system.alert.config;

import com.coelhotechne.detection_system.cluster.bus.ClusterEventBus;
import com.coelhotechne.detection_system.security.event.SessionRevocationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class SessionRevocationListener {
    private final StompSessionRegistry registry;
    private final ClusterEventBus bus;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT,fallbackExecution = true)
    public void onRevocation(SessionRevocationEvent event){
        registry.closeSessionsOf(event.principals());
        bus.revokeSessions(event.principals());
    }
}
