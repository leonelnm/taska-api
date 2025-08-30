package com.codigozerocuatro.taska.infra.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

public record CrearUserRequest(
    @NotBlank(message = "is required")
    @Length(min = 4, message = "must be at least 4 characters")
    @Length(max = 20, message = "must be less than 20 characters")
    @Pattern(regexp = "^[a-z0-9_-]*$", message = "can only contain lowercase letters, numbers, underscores, and hyphens")
    String username,

    @NotBlank(message = "is required")
    @Length(min = 8, message = "must be at least 8 characters")
    @Pattern(regexp = "^\\S*$", message = "must not contain spaces")
    String password,

    String nombre,

    @NotNull(message = "is required")
    Long puestoId
) {
}
