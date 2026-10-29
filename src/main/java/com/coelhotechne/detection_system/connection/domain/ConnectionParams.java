package com.coelhotechne.detection_system.connection.domain;


import com.coelhotechne.detection_system.connection.domain.enums.LinkType;

public record ConnectionParams(
        String host,
        Integer port,
        String streamUri,
        String credentialRef,
        LinkType linkType,
        Integer signalStrengthDbm
) {
    public ConnectionParams {
        if (port != null && (port < 1 || port > 65535)) {
            throw new IllegalArgumentException("port fora do intervalo válido: " + port);
        }
        if (linkType != null && !linkType.isWireless() && signalStrengthDbm != null) {
            throw new IllegalArgumentException("signalStrengthDbm só é válido para linkType wireless");
        }
    }

    public static ConnectionParams rtsp(String streamUri, LinkType linkType, String credentialRef) {
        return new ConnectionParams(null, null, streamUri, credentialRef, linkType, null);
    }

    public static ConnectionParams onvif(String host, int port, String credentialRef, LinkType linkType) {
        return new ConnectionParams(host, port, null, credentialRef, linkType, null);
    }

    public static ConnectionParams mqtt(String host, int port, String credentialRef, LinkType linkType) {
        return new ConnectionParams(host, port, null, credentialRef, linkType, null);
    }

    public static ConnectionParams http(String host, int port, String credentialRef, LinkType linkType) {
        return new ConnectionParams(host, port, null, credentialRef, linkType, null);
    }

    public static ConnectionParams webSocket(String streamUri, String credentialRef, LinkType linkType) {
        return new ConnectionParams(null, null, streamUri, credentialRef, linkType, null);
    }

    public ConnectionParams withSignal(int dbm) {
        return new ConnectionParams(host, port, streamUri, credentialRef, linkType, dbm);
    }
}
