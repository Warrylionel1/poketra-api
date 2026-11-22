package com.poketra.dto.request;

import com.poketra.model.AccountType;
import com.poketra.model.Currency;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateAccountRequest(@NotBlank @Size(max = 100) String name, @NotNull AccountType type,
                                   @NotNull Currency currency,
                                   @Digits(integer = 17, fraction = 2) BigDecimal initialBalance) {
}