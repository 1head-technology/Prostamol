package com.prostamol.Prostamol.domain.port.in.budget;

import com.prostamol.Prostamol.domain.model.budget.Budget;
import com.prostamol.Prostamol.domain.model.budget.BudgetStatus;

import java.time.LocalDate;
import java.util.UUID;

public interface UpdateBudgetUseCase {
    Budget execute(Command command);

    record Command(
        UUID userId,
        UUID budgetId,
        String name,
        LocalDate from,
        LocalDate to,
        BudgetStatus status
    ) {}
}
