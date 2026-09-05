package com.prostamol.Prostamol.infrastructure.web.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateBudgetLineRequest(
    UUID categoryId,
    @Positive BigDecimal plannedAmount,
    @Size(min = 3, max = 3) String currency
) {}
