package com.coelhotechne.detection_system.cam.infrastructure.protocol;

import com.coelhotechne.detection_system.cam.domain.ptz.CamPtz;
import com.coelhotechne.detection_system.cam.domain.ptz.PtzCommandSender;
import com.coelhotechne.detection_system.cam.domain.ptz.enums.PtzAction;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class LoggingPtzCommandSender implements PtzCommandSender {
    @Override
    public void send(CamPtz camPtz, PtzAction action, Float value) {
        log.info("PTZ {} {} for camera: {} (no PTZ driver configured: command recorded only)",
                action,value==null ?"":value,camPtz.getUuid() );
    }
}
