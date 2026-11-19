package com.poketra.controller;

import com.poketra.dto.request.GoogleAuthRequest;
import com.poketra.dto.response.AuthResponse;
import com.poketra.exception.InvalidGoogleTokenException;
import com.poketra.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/google")
    public AuthResponse authenticateWithGoogle(@Valid @RequestBody GoogleAuthRequest request)
            throws InvalidGoogleTokenException {
        return authService.authenticateWithGoogle(request.idToken());
    }
}