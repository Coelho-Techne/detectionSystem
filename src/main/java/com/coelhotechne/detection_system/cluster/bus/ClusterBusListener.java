package com.coelhotechne.detection_system.cluster.bus;

public interface ClusterBusListener {
    void onMessage(byte[] payload);
}
