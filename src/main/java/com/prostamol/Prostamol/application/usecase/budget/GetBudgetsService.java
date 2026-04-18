package com.prostamol.Prostamol.application.usecase.budget;

import com.prostamol.Prostamol.domain.model.budget.Budget;
import com.prostamol.Prostamol.domain.port.in.budget.GetBudgetsUseCase;
import com.prostamol.Prostamol.domain.port.out.BudgetRepositoryPort;

import java.util.List;
import java.util.UUID;

public class GetBudgetsService implements GetBudgetsUseCase {

    private final BudgetRepositoryPort budgetRepository;

    public GetBudgetsService(BudgetRepositoryPort budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    @Override
    public List<Budget> execute(UUID userId) {
        return budgetRepository.findAllByUserId(userId);
    }
}
