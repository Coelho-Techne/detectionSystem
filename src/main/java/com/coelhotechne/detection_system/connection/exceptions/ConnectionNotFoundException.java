package com.coelhotechne.detection_system.connection.exceptions;


import com.coelhotechne.detection_system.connection.exceptions.enums.ConnectionError;
import lombok.Getter;

@Getter
public class ConnectionNotFoundException extends RuntimeException {
     private final ConnectionError error;
    public ConnectionNotFoundException(ConnectionError error, String message) {
        super(message);
        this.error = error;
    }
}
