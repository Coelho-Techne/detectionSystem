package com.coelhotechne.detection_system.connection.api.controller;

import com.coelhotechne.detection_system.connection.exceptions.ConnectionNotFoundException;
import com.coelhotechne.detection_system.globalClass.exceptions.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ConnectionExceptionHandler {

    @ExceptionHandler(ConnectionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handle(ConnectionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ErrorResponse(ex.getError().name(), ex.getMessage()));
    }
}
