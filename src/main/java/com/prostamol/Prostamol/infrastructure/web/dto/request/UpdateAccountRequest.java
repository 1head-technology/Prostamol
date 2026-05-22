package com.prostamol.Prostamol.infrastructure.web.dto.request;

import com.prostamol.Prostamol.domain.model.account.AccountType;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record UpdateAccountRequest(
    String name,
    AccountType type,
    @PositiveOrZero BigDecimal initialBalance,
    @Size(min = 3, max = 3) String currency
) {}
