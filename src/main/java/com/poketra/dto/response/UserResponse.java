package com.poketra.dto.response;

import java.util.UUID;

public record UserResponse(UUID id, String email, String firstName, String lastName, String pictureUrl,
                           @jakarta.validation.constraints.Size(max = 3) @jakarta.validation.constraints.NotNull String currency) {
}
