package com.codigozerocuatro.taska.infra.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

public record AdminChangePasswordRequest(
        String username,

        @NotBlank(message = "is required")
        @Length(min = 8, message = "must be at least 8 characters")
        @Pattern(regexp = "^\\S*$", message = "must not contain spaces")
        String password) {
}
