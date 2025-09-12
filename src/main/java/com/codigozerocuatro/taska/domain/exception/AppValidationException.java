package com.codigozerocuatro.taska.domain.exception;

import com.codigozerocuatro.taska.domain.model.ErrorCode;
import lombok.Getter;

import java.util.Map;

@Getter
public class AppValidationException extends RuntimeException {
    private final Map<String, String> errors;

    public AppValidationException(String fieldName, String error) {
        super(ErrorCode.VALIDATION_FAILED);
        this.errors = Map.of(fieldName, error);
    }

    public AppValidationException(Map<String, String> errors) {
        super(ErrorCode.VALIDATION_FAILED);
        this.errors = errors;
    }
}
