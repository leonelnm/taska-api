package com.codigozerocuatro.taska.infra.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;


@Validated
@ConfigurationProperties(prefix = "app")
public record AppProperties(
        @Valid Jwt jwt,
        @Valid User user,
        Cookie cookie

) {
    public record User(
            @Length(min = 8, message = "Default password must be at least 8 characters")
            String defaultPassword
    ){}

    public record Jwt(
            @NotBlank(message = "JWT secret cannot be blank")
            String secret,

            @Min(value = 60000, message = "JWT expiration must be at least 1 minute")
            long expiration,

            @Min(value = 300000, message = "JWT refresh expiration must be at least 5 minutes")
            long refreshExpiration
    ) {}

    public record Cookie(
            boolean secure,
            String sameSite,
            @Min(value = 1, message = "Days to expire must be at least 1 day")
            int daysToExpire
    ){}

}
