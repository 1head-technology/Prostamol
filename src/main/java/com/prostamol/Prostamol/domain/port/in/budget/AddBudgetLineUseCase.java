package com.prostamol.Prostamol.domain.port.in.budget;

import com.prostamol.Prostamol.domain.model.budget.Budget;
import com.prostamol.Prostamol.domain.model.shared.Money;

import java.util.UUID;

public interface AddBudgetLineUseCase {
    Budget execute(Command command);

    record Command(
        UUID budgetId,
        UUID categoryId,
        Money plannedAmount
    ) {}
}
