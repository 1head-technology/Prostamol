package com.prostamol.Prostamol.infrastructure.web.dto.response;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record BudgetSummaryResponse(
    UUID budgetId,
    String budgetName,
    List<LineSummaryResponse> lines
) {
    public record LineSummaryResponse(
        UUID categoryId,
        String categoryName,
        BigDecimal planned,
        BigDecimal spent,
        BigDecimal remaining,
        String currency
    ) {}
}
