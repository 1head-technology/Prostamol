package com.prostamol.Prostamol.infrastructure.web.dto.response;

import com.prostamol.Prostamol.domain.model.account.AccountType;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponse(
    UUID id,
    UUID userId,
    String name,
    AccountType type,
    BigDecimal initialBalance,
    String currency
) {}
