package com.codigozerocuatro.taska.infra.dto;

import com.codigozerocuatro.taska.domain.model.ErrorCode;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record LoginRequest(
        @NotNull(message = ErrorCode.USERNAME_REQUIRED)
        String username,

        @NotNull(message = ErrorCode.PASSWORD_REQUIRED)
        @Length(max = 100, message = ErrorCode.PASSWORD_MAX_LENGTH)
        String password) {
}
