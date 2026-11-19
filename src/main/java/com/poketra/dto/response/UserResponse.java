package com.poketra.dto.response;

import com.poketra.model.Currency;

import java.util.UUID;

public record UserResponse(UUID id, String email, String firstName, String lastName, String pictureUrl,
                           Currency currency) {
}
