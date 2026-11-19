package com.poketra.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@ConfigurationProperties(prefix = "app")
@Validated
public record AppProperties(@Valid @NotNull Google google, @Valid @NotNull Jwt jwt, @Valid @NotNull Cors cors) {
    public record Google(@NotBlank String clientId) {

    }

    public record Jwt(@NotBlank @Size(min = 32) String secret, @Positive long expirationMinutes) {

    }

    public record Cors(@NotEmpty List<String> allowedOrigins) {

    }
}
