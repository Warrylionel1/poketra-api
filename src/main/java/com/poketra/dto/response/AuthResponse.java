package com.poketra.dto.response;

public record AuthResponse(String token, UserResponse user) {
}
