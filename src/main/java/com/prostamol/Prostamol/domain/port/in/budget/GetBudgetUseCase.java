package com.prostamol.Prostamol.domain.port.in.budget;

import com.prostamol.Prostamol.domain.model.budget.Budget;

import java.util.UUID;

public interface GetBudgetUseCase {
    Budget execute(UUID userId, UUID budgetId);
}
