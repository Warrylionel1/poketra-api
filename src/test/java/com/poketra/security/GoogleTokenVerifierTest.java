package com.poketra.security;

import com.poketra.config.AppProperties;
import com.poketra.exception.InvalidGoogleTokenException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class GoogleTokenVerifierTest {

    @Test
    void rejectsMalformedToken() {
        AppProperties properties = new AppProperties(
                new AppProperties.Google("test-client-id"),
                new AppProperties.Jwt("x".repeat(32), 60),
                new AppProperties.Cors(List.of("http://localhost:3000"))
        );
        GoogleTokenVerifier verifier = new GoogleTokenVerifier(properties);

        Assertions.assertThrows(InvalidGoogleTokenException.class, () -> verifier.verify("not-a-jwt"));
    }
}