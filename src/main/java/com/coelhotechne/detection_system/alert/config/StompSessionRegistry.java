package com.coelhotechne.detection_system.alert.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Configuration
public class StompSessionRegistry implements WebSocketHandlerDecoratorFactory {
    private final Map<String, WebSocketSession>sessions = new ConcurrentHashMap<>();
    private final Map<String, String>principalBySession = new ConcurrentHashMap<>();

    @Override
    public WebSocketHandler decorate(WebSocketHandler handler) {
        return new WebSocketHandlerDecorator(handler){
            @Override
            public void afterConnectionEstablished(WebSocketSession session)throws Exception{
                sessions.put(session.getId(),session);
                super.afterConnectionEstablished(session);
            }
            @Override
            public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus)throws Exception{
                sessions.remove(session.getId());
                principalBySession.remove(session.getId());
                super.afterConnectionClosed(session,closeStatus);
            }

        };
    }


    public void bind(String sessionId,String principal){
        if (sessionId !=null && principal !=null){
            principalBySession.put(sessionId,principal);
        }
    }
    public int closeSessionsOf(Collection<String> principals){
        int closed = 0;
        for (Map.Entry<String, String>entry:principalBySession.entrySet()){
            if (!principals.contains(entry.getValue())){
                continue;
            }
            WebSocketSession session = sessions.get(entry.getKey());
            principalBySession.remove(entry.getKey());
            if (session==null){
                continue;
            }
            try {
                session.close(CloseStatus.POLICY_VIOLATION.withReason("Credentials revoked"));
                closed++;
            }catch (IOException ex){
                log.debug("Failed to close STOMP session {}",entry.getKey(),ex);
            }
        }
        if (closed>0){
            log.info("{} STOMP session(s) closed after credential revocation",closed);
        }
        return closed;
    }

}
