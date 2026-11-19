package com.poketra.service;

import com.poketra.dto.response.AuthResponse;
import com.poketra.exception.InvalidGoogleTokenException;
import com.poketra.model.Currency;
import com.poketra.model.User;
import com.poketra.repository.UserRepository;
import com.poketra.security.GoogleTokenVerifier;
import com.poketra.security.GoogleUserInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

class AuthServiceTest {

    private GoogleTokenVerifier googleTokenVerifier;
    private UserRepository userRepository;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        googleTokenVerifier = Mockito.mock(GoogleTokenVerifier.class);
        userRepository = Mockito.mock(UserRepository.class);
        JwtService jwtService = Mockito.mock(JwtService.class);
        authService = new AuthService(googleTokenVerifier, userRepository, jwtService);

        Mockito.when(jwtService.generateToken(Mockito.any(), Mockito.anyString())).thenReturn("jwt-token");
    }

    @Test
    void createsUserOnFirstLogin() throws InvalidGoogleTokenException {
        GoogleUserInfo info = new GoogleUserInfo("g-123", "warry@example.com", "Warry", "Lionel", null);
        Mockito.when(googleTokenVerifier.verify("google-token")).thenReturn(info);
        Mockito.when(userRepository.findByGoogleId("g-123")).thenReturn(Optional.empty());
        Mockito.when(userRepository.save(Mockito.any(User.class)))
                .thenAnswer(invocation -> {
                    User u = invocation.getArgument(0);
                    u.setId(java.util.UUID.randomUUID());
                    return u;
                });

        AuthResponse response = authService.authenticateWithGoogle("google-token");

        Assertions.assertEquals("jwt-token", response.token());
        Assertions.assertEquals("warry@example.com", response.user().email());
        Assertions.assertEquals(Currency.MGA, response.user().currency());
        Assertions.assertNotNull(response.user().id());
        Mockito.verify(userRepository).save(Mockito.any(User.class));
    }

    @Test
    void reusesExistingUserOnNextLogin() throws InvalidGoogleTokenException {
        GoogleUserInfo info = new GoogleUserInfo("g-123", "warry@example.com", "Warry", "Lionel", null);
        User existing = User.builder()
                .id(java.util.UUID.randomUUID())
                .googleId("g-123")
                .email("warry@example.com")
                .firstName("Warry")
                .currency(Currency.MGA)
                .build();
        Mockito.when(googleTokenVerifier.verify("google-token")).thenReturn(info);
        Mockito.when(userRepository.findByGoogleId("g-123")).thenReturn(Optional.of(existing));

        AuthResponse response = authService.authenticateWithGoogle("google-token");

        Assertions.assertEquals("jwt-token", response.token());
        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User.class));
    }

    @Test
    void rejectsInvalidGoogleToken() throws InvalidGoogleTokenException {
        Mockito.when(googleTokenVerifier.verify("bad-token"))
                .thenThrow(new InvalidGoogleTokenException("Invalid Google token"));

        Assertions.assertThrows(InvalidGoogleTokenException.class,
                () -> authService.authenticateWithGoogle("bad-token"));

        Mockito.verify(userRepository, Mockito.never()).save(Mockito.any(User.class));
    }
}