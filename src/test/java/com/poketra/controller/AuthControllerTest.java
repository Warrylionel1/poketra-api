package com.poketra.controller;

import com.poketra.dto.response.AuthResponse;
import com.poketra.dto.response.UserResponse;
import com.poketra.exception.GlobalExceptionHandler;
import com.poketra.exception.InvalidGoogleTokenException;
import com.poketra.model.Currency;
import com.poketra.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

class AuthControllerTest {

    private AuthService authService;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        authService = Mockito.mock(AuthService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void returnsTokenForValidGoogleToken() throws Exception {
        UserResponse user = new UserResponse(
                UUID.randomUUID(), "warry@example.com", "Warry", "Lionel", null, Currency.MGA);
        Mockito.when(authService.authenticateWithGoogle("good-token"))
                .thenReturn(new AuthResponse("jwt-token", user));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\":\"good-token\"}"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").value("jwt-token"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.email").value("warry@example.com"));
    }

    @Test
    void returns401ForInvalidGoogleToken() throws Exception {
        Mockito.when(authService.authenticateWithGoogle("bad-token"))
                .thenThrow(new InvalidGoogleTokenException("Invalid Google token"));

        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\":\"bad-token\"}"))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(401));
    }

    @Test
    void returns400WhenTokenIsBlank() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/api/auth/google")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"idToken\":\"\"}"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}