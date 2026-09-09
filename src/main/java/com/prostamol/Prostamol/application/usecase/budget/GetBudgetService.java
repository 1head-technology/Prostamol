package com.prostamol.Prostamol.application.usecase.budget;

import com.prostamol.Prostamol.domain.model.budget.Budget;
import com.prostamol.Prostamol.domain.port.in.budget.GetBudgetUseCase;
import com.prostamol.Prostamol.domain.port.out.BudgetRepositoryPort;
import org.springframework.security.access.AccessDeniedException;

import java.util.UUID;

public class GetBudgetService implements GetBudgetUseCase {

    private final BudgetRepositoryPort budgetRepository;

    public GetBudgetService(BudgetRepositoryPort budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    @Override
    public Budget execute(UUID userId, UUID budgetId) {
        Budget budget = budgetRepository
            .findById(budgetId)
            .orElseThrow(() -> new IllegalArgumentException("Budget not found: " + budgetId));

        if (!budget.getUserId().equals(userId)) {
            throw new AccessDeniedException("Budget does not belong to the authenticated user");
        }

        return budget;
    }
}
