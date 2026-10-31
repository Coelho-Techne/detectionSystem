package com.coelhotechne.detection_system.cam.domain.ptz;

import com.coelhotechne.detection_system.cam.domain.ptz.enums.PtzAction;

public interface PtzCommandSender {

    void send(CamPtz camPtz, PtzAction action,Float value);
    class PtzCommandException extends RuntimeException{
        public PtzCommandException(String message, Throwable cause){
            super(message,cause);
        }
    }
}
