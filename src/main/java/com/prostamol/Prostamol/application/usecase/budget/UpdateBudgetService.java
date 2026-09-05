package com.prostamol.Prostamol.application.usecase.budget;

import com.prostamol.Prostamol.domain.model.budget.Budget;
import com.prostamol.Prostamol.domain.model.shared.DateRange;
import com.prostamol.Prostamol.domain.port.in.budget.UpdateBudgetUseCase;
import com.prostamol.Prostamol.domain.port.out.BudgetRepositoryPort;

public class UpdateBudgetService implements UpdateBudgetUseCase {

    private final BudgetRepositoryPort budgetRepository;

    public UpdateBudgetService(BudgetRepositoryPort budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    @Override
    public Budget execute(Command command) {
        Budget budget = budgetRepository
            .findById(command.budgetId())
            .orElseThrow(() -> new IllegalArgumentException("Budget not found: " + command.budgetId()));

        DateRange currentPeriod = budget.getPeriod();
        DateRange updatedPeriod = new DateRange(
            command.from() != null ? command.from() : currentPeriod.from(),
            command.to() != null ? command.to() : currentPeriod.to()
        );

        return budgetRepository.save(budget.update(
            command.name() != null ? command.name() : budget.getName(),
            updatedPeriod,
            command.status() != null ? command.status() : budget.getStatus()
        ));
    }
}
