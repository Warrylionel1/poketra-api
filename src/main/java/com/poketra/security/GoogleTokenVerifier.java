package com.poketra.security;

import com.poketra.config.AppProperties;
import com.poketra.exception.InvalidGoogleTokenException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class GoogleTokenVerifier {

    private final Set<String> googleIssuers = Set.of("https://accounts.google.com", "accounts.google.com");

    private final JwtDecoder decoder;

    public GoogleTokenVerifier(AppProperties properties) {
        String clientId = properties.google().clientId();

        String googleJWTsUri = "https://www.googleapis.com/oauth2/v3/certs";
        NimbusJwtDecoder nimbusDecoder = NimbusJwtDecoder.withJwkSetUri(googleJWTsUri).build();
        nimbusDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(),
                issuerValidator(),
                audienceValidator(clientId)
        ));
        this.decoder = nimbusDecoder;
    }

    public void verify(String idToken) throws InvalidGoogleTokenException {
        Jwt jwt;
        try {
            jwt = decoder.decode(idToken);
        } catch (JwtException e) {
            log.warn("Google token rejected: {}", e.getMessage());
            throw new InvalidGoogleTokenException("Invalid Google token", e);
        }

        Boolean emailVerified = jwt.getClaimAsBoolean("email_verified");
        if (emailVerified == null || !emailVerified) {
            throw new InvalidGoogleTokenException("Google email is not verified");
        }

        String email = jwt.getClaimAsString("email");
        if (email == null || email.isBlank()) {
            throw new InvalidGoogleTokenException("Google token has no email");
        }

        new GoogleUserInfo(
                jwt.getSubject(),
                email,
                resolveFirstName(jwt, email),
                jwt.getClaimAsString("family_name"),
                jwt.getClaimAsString("picture")
        );
    }

    private String resolveFirstName(Jwt jwt, String email) {
        String givenName = jwt.getClaimAsString("given_name");
        if (givenName != null && !givenName.isBlank()) {
            return givenName;
        }
        String fullName = jwt.getClaimAsString("name");
        if (fullName != null && !fullName.isBlank()) {
            return fullName;
        }
        return email.substring(0, email.indexOf('@'));
    }

    private OAuth2TokenValidator<Jwt> issuerValidator() {
        return jwt -> {
            String issuer = jwt.getClaimAsString(JwtClaimNames.ISS);
            if (issuer != null && googleIssuers.contains(issuer)) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Invalid issuer", null));
        };
    }

    private OAuth2TokenValidator<Jwt> audienceValidator(String clientId) {
        return jwt -> {
            List<String> audience = jwt.getAudience();
            if (audience != null && audience.contains(clientId)) {
                return OAuth2TokenValidatorResult.success();
            }
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_token", "Invalid audience", null));
        };
    }
}