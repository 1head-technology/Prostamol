package com.prostamol.Prostamol.infrastructure.web.dto.response;

import com.prostamol.Prostamol.domain.model.budget.BudgetStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record BudgetResponse(
    UUID id,
    UUID userId,
    String name,
    LocalDate from,
    LocalDate to,
    BudgetStatus status,
    List<BudgetLineResponse> lines
) {
    public record BudgetLineResponse(UUID id, UUID categoryId, BigDecimal plannedAmount, String currency) {}
}
