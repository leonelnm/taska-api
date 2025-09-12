package com.codigozerocuatro.taska.infra.dto;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse(HttpStatus status,
                            String message,
                            LocalDateTime timestamp,
                            Map<String, String> details) {

    public ErrorResponse(HttpStatus status, String message){
        this(status, message, LocalDateTime.now(), Map.of());
    }

    public ErrorResponse(HttpStatus status, String message, Map<String, String> details){
        this(status, message, LocalDateTime.now(), details);
    }

}
