package com.prostamol.Prostamol.domain.port.in.budget;

import com.prostamol.Prostamol.domain.model.shared.Money;

import java.util.List;
import java.util.UUID;

public interface GetBudgetSummaryUseCase {
    BudgetSummary execute(UUID budgetId);

    record BudgetSummary(UUID budgetId, String budgetName, List<LineSummary> lines) {}

    record LineSummary(
        UUID categoryId,
        String categoryName,
        Money planned,
        Money spent,
        Money remaining
    ) {}
}
