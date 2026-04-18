package com.prostamol.Prostamol.infrastructure.web.dto.request;

import com.prostamol.Prostamol.domain.model.account.AccountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateAccountRequest(
    @NotBlank String name,
    @NotNull AccountType type,
    @NotNull @PositiveOrZero BigDecimal initialBalance,
    @NotBlank @Size(min = 3, max = 3) String currency
) {}
