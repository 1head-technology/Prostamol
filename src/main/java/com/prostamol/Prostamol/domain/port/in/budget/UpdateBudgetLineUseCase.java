package com.prostamol.Prostamol.domain.port.in.budget;

import com.prostamol.Prostamol.domain.model.budget.Budget;

import java.math.BigDecimal;
import java.util.UUID;

public interface UpdateBudgetLineUseCase {
    Budget execute(Command command);

    record Command(
        UUID budgetId,
        UUID lineId,
        UUID categoryId,
        BigDecimal plannedAmount,
        String currency
    ) {}
}
