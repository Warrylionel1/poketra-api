package com.poketra.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "app")
@Validated
public record AppProperties(@Valid @NotNull Google google, @Valid @NotNull Jwt jwt) {
    public record Google(@NotBlank String clientId) {

    }

    public record Jwt(@NotBlank @Size(min = 32) String secret, @Positive long expirationMinutes) {

    }
}
