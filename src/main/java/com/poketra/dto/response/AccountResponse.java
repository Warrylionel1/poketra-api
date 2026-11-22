package com.poketra.dto.response;

import com.poketra.model.AccountType;
import com.poketra.model.Currency;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AccountResponse(UUID id, String name, AccountType type, Currency currency, BigDecimal initialBalance,
                              boolean archived, OffsetDateTime createdAt) {
}