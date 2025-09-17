package com.codigozerocuatro.taska.infra.dto;

import com.codigozerocuatro.taska.domain.model.ErrorCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.hibernate.validator.constraints.Length;

public record ChangePasswordRequest(
        @NotBlank(message = ErrorCode.PASSWORD_REQUIRED)
        String currentPassword,

        @NotBlank(message = ErrorCode.PASSWORD_REQUIRED)
        @Length(min = 8, message = ErrorCode.PASSWORD_MIN_LENGTH)
        @Pattern(regexp = "^\\S*$", message = ErrorCode.PASSWORD_NO_SPACES)
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                message = ErrorCode.PASSWORD_COMPLEXITY
        )
        String newPassword
) {
}
