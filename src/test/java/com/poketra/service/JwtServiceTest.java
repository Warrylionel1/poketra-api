package com.poketra.service;

import com.poketra.config.AppProperties;
import com.poketra.config.JwtConfig;
import com.poketra.exception.InvalidJwtException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

class JwtServiceTest {

    private JwtService buildService(String secret) {
        AppProperties properties = new AppProperties(
                new AppProperties.Google("test-client-id"),
                new AppProperties.Jwt(secret, 60)
        );
        JwtConfig config = new JwtConfig();
        return new JwtService(config.jwtEncoder(properties), config.jwtDecoder(properties), properties);
    }

    @Test
    void generatedTokenCanBeReadBack() throws InvalidJwtException {
        JwtService service = buildService("a".repeat(32));
        UUID userId = UUID.randomUUID();

        String token = service.generateToken(userId, "warry@example.com");

        Assertions.assertEquals(userId, service.extractUserId(token));
    }

    @Test
    void rejectsTokenSignedWithAnotherSecret() {
        JwtService issuer = buildService("a".repeat(32));
        JwtService other = buildService("b".repeat(32));

        String token = issuer.generateToken(UUID.randomUUID(), "warry@example.com");

        Assertions.assertThrows(InvalidJwtException.class, () -> other.extractUserId(token));
    }

    @Test
    void rejectsMalformedToken() {
        JwtService service = buildService("a".repeat(32));

        Assertions.assertThrows(InvalidJwtException.class, () -> service.extractUserId("not-a-jwt"));
    }
}