package com.coelhotechne.detection_system.cluster.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.net.InetAddress;
import java.time.Duration;
import java.util.UUID;

@ConfigurationProperties(prefix = "cluster")
public record ClusterProperties(
        Boolean enabled,
        String nodeId,
        Duration heartbeatInterval,
        Duration nodeTimeout,
        String busTopic,
        String mqttShareGroup
) {
    public ClusterProperties{
        enabled = enabled != null ? enabled : Boolean.FALSE;
        nodeId = nodeId != null && !nodeId.isBlank() ? nodeId.trim() : defaultNodeId();
        heartbeatInterval = heartbeatInterval !=null ? heartbeatInterval:Duration.ofSeconds(10);
        nodeTimeout = nodeTimeout != null ? nodeTimeout : Duration.ofSeconds(30);
        busTopic = busTopic != null && !busTopic.isBlank() ? busTopic.trim() :"detection-system/cluster/events";
        mqttShareGroup = mqttShareGroup != null && !mqttShareGroup.isBlank() ? mqttShareGroup.trim() : "detection-system";

        if (!nodeId.matches("[A-Za-z0-9._-]{1,64}")){
            throw new IllegalArgumentException("cluster.node-id must match [A-Za-z0-9._-]{1,64}: "+nodeId);
        }
        if (nodeTimeout.compareTo(heartbeatInterval.multipliedBy(2))<0){
            throw new IllegalArgumentException("cluster.node-timeout must be at least 2x cluster.heartbeat-interval");
        }
        if (busTopic.contains("+")||busTopic.contains("#")){
            throw new IllegalArgumentException("cluster.bus-topic must not contain MQTT wildcards");
        }if (!mqttShareGroup.matches("[A-Za-z0-9._-]{1,64}")){
            throw new IllegalArgumentException("cluster.mqtt-share-group must match [A-Za-z0-9._-]{1,64}");
        }
    }
    public boolean isEnabled(){return Boolean.TRUE.equals(enabled);}
    private static String defaultNodeId(){
        String host;
        try {
            host = InetAddress.getLocalHost().getHostName();
        }catch (Exception ex){
            host = "node";
        }
        String safe = host.replaceAll("[^A-Za-z0-9._-]", "-");
        if (safe.length()>50){
            safe = safe.substring(0,50);
        }
        return safe+"-"+ UUID.randomUUID().toString()
                .substring(0,8);
    }
}
