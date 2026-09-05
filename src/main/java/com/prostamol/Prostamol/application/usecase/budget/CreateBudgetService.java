package com.prostamol.Prostamol.application.usecase.budget;

import com.prostamol.Prostamol.domain.model.budget.Budget;
import com.prostamol.Prostamol.domain.model.budget.BudgetLine;
import com.prostamol.Prostamol.domain.model.budget.BudgetStatus;
import com.prostamol.Prostamol.domain.port.in.budget.CreateBudgetUseCase;
import com.prostamol.Prostamol.domain.port.out.BudgetRepositoryPort;
import com.prostamol.Prostamol.domain.port.out.CategoryRepositoryPort;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CreateBudgetService implements CreateBudgetUseCase {

    private final BudgetRepositoryPort budgetRepository;
    private final CategoryRepositoryPort categoryRepository;

    public CreateBudgetService(
        BudgetRepositoryPort budgetRepository,
        CategoryRepositoryPort categoryRepository
    ) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Budget execute(Command command) {
        List<BudgetLine> lines = command.lines().stream()
            .map(lc -> {
                categoryRepository
                    .findById(lc.categoryId())
                    .filter(category -> category.isSystem() || command.userId().equals(category.getUserId()))
                    .orElseThrow(() -> new IllegalArgumentException("Category not found: " + lc.categoryId()));
                return new BudgetLine(UUID.randomUUID(), lc.categoryId(), lc.plannedAmount());
            })
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
