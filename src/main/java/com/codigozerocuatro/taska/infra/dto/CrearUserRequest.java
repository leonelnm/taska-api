package com.codigozerocuatro.taska.infra.dto;

import com.codigozerocuatro.taska.domain.model.ErrorCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

public record CrearUserRequest(
        @NotBlank(message = ErrorCode.USERNAME_REQUIRED)
        @Length(min = 4, message = ErrorCode.USERNAME_MIN_LENGTH)
        @Length(max = 20, message = ErrorCode.USERNAME_MAX_LENGTH)
        @Pattern(regexp = "^[a-z0-9_-]*$", message = ErrorCode.USERNAME_INVALID_CHARS)
        String username,

        @NotBlank(message = ErrorCode.PASSWORD_REQUIRED)
        @Length(min = 8, message = ErrorCode.PASSWORD_MIN_LENGTH)
        @Pattern(regexp = "^\\S*$", message = ErrorCode.PASSWORD_NO_SPACES)
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                message = ErrorCode.PASSWORD_COMPLEXITY
        )
        String password,

        String nombre,

        @NotNull(message = ErrorCode.PUESTO_REQUIRED)
        Long puestoId
) {}

