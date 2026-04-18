package com.prostamol.Prostamol.infrastructure.web.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateBudgetRequest(
    @NotBlank String name,
    @NotNull LocalDate from,
    @NotNull LocalDate to,
    @NotNull @Valid List<BudgetLineRequest> lines
) {
    public record BudgetLineRequest(
        @NotNull UUID categoryId,
        @NotNull @Positive BigDecimal plannedAmount,
        @NotBlank @Size(min = 3, max = 3) String currency
    ) {}
}
