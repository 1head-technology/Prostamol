package com.prostamol.Prostamol.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record AddBudgetLineRequest(
    @NotNull UUID categoryId,
    @NotNull @Positive BigDecimal plannedAmount,
    @NotBlank @Size(min = 3, max = 3) String currency
) {}
