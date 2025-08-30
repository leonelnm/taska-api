package com.codigozerocuatro.taska.infra.dto;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(HttpStatus status,
                            String message,
                            LocalDateTime timestamp,
                            List<String> details) {

    public ErrorResponse(HttpStatus status, String message){
        this(status, message, LocalDateTime.now(), List.of());
    }

    public ErrorResponse(HttpStatus status, String message, List<String> details){
        this(status, message, LocalDateTime.now(), details);
    }

}
