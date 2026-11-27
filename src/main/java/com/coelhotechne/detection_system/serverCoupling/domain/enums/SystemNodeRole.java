package com.coelhotechne.detection_system.serverCoupling.domain.enums;

public enum SystemNodeRole {
    EDGE_GATEWAY,
    DRONE_CONTROLLER,
    VIDEO_RECORDER,
    WORKSTATION,
    SERVER,
    MOBILE,
    OTHER;

    public String authority(){
        return "NODE_ROLE_"+name();
    }
}
