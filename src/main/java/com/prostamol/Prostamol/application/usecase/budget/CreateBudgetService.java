package com.prostamol.Prostamol.application.usecase.budget;

import com.prostamol.Prostamol.domain.model.budget.Budget;
import com.prostamol.Prostamol.domain.model.budget.BudgetLine;
import com.prostamol.Prostamol.domain.model.budget.BudgetStatus;
import com.prostamol.Prostamol.domain.port.in.budget.CreateBudgetUseCase;
import com.prostamol.Prostamol.domain.port.out.BudgetRepositoryPort;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CreateBudgetService implements CreateBudgetUseCase {

    private final BudgetRepositoryPort budgetRepository;

    public CreateBudgetService(BudgetRepositoryPort budgetRepository) {
        this.budgetRepository = budgetRepository;
    }

    @Override
    public Budget execute(Command command) {
        List<BudgetLine> lines = command.lines().stream()
            .map(lc -> new BudgetLine(UUID.randomUUID(), lc.categoryId(), lc.plannedAmount()))
            .collect(Collectors.toList());

        return budgetRepository.save(new Budget(
            UUID.randomUUID(),
            command.userId(),
            command.name(),
            command.period(),
            BudgetStatus.ACTIVE,
            lines
        ));
    }
}
