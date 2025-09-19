package com.codigozerocuatro.taska.domain.exception;

import com.codigozerocuatro.taska.domain.model.ErrorCode;
import lombok.Getter;

import java.util.Map;

@Getter
public class AppEntityNotFoundException extends RuntimeException {
    private final String value;

    public AppEntityNotFoundException(Long value) {
        super(ErrorCode.ENTITY_NOT_FOUND);
        this.value = value != null ? value.toString() : null;
    }

    public AppEntityNotFoundException(String value) {
        super(ErrorCode.ENTITY_NOT_FOUND);
        this.value = value;
    }

}
