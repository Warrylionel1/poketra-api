package com.poketra.security;

import com.poketra.config.AppProperties;
import com.poketra.exception.InvalidGoogleTokenException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GoogleTokenVerifierTest {

    @Test
    void rejectsMalformedToken() {
        AppProperties properties = new AppProperties(
                new AppProperties.Google("test-client-id"),
                new AppProperties.Jwt("x".repeat(32), 60)
        );
        GoogleTokenVerifier verifier = new GoogleTokenVerifier(properties);

        Assertions.assertThrows(InvalidGoogleTokenException.class, () -> verifier.verify("not-a-jwt"));
    }
}