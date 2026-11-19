package com.coelhotechne.detection_system.cluster.bus;


import com.coelhotechne.detection_system.alert.config.StompSessionRegistry;
import com.coelhotechne.detection_system.alert.notification.StompAlertChannel;
import com.coelhotechne.detection_system.cluster.config.ClusterProperties;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Log4j2
@Component
@AllArgsConstructor
public class ClusterBusDispatcher implements ClusterBusListener{

    private final ObjectMapper mapper;
    private final ClusterProperties properties;
    private final StompAlertChannel alertChannel;
    private final StompSessionRegistry sessionRegistry;


    @Override
    public void onMessage(byte[] payload) {
        ClusterMessage message;
        try {
            message = mapper.readValue(payload,ClusterMessage.class);
        }catch (JacksonException ex){
            log.warn("Cluster bus: unreadable message discarded ({})",ex.getOriginalMessage());
            return;
        }
        if (message==null|| properties.nodeId().equals(message.origin())){
            return;
        }
        switch (message.type()){
            case ALERT_BROADCAST -> {
                if (message.alert()!=null){
                    alertChannel.broadcast(message.alert());
                }
            }
            case ALERT_DELIVER -> {
                if (message.alert() !=null){
                    alertChannel.deliverToDevices(message.alert(),message.deviceIds());
                }
            }
            case SESSIONS_REVOKED -> sessionRegistry.closeSessionsOf(message.principals());
        }
    }
}
