package com.prostamol.Prostamol.application.usecase.budget;

import com.prostamol.Prostamol.domain.model.budget.Budget;
import com.prostamol.Prostamol.domain.port.in.budget.GetBudgetUseCase;
import com.prostamol.Prostamol.domain.port.out.BudgetRepositoryPort;

import java.util.UUID;

public class GetBudgetService implements GetBudgetUseCase {

    private final BudgetRepositoryPort budgetRepository;

    public GetBudgetService(BudgetRepositoryPort budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    @Override
    public Budget execute(UUID budgetId) {
        return budgetRepository
            .findById(budgetId)
            .orElseThrow(() -> new IllegalArgumentException("Budget not found: " + budgetId));
    }
}
