package com.prostamol.Prostamol.application.usecase.budget;

import com.prostamol.Prostamol.domain.model.budget.Budget;
import com.prostamol.Prostamol.domain.model.budget.BudgetLine;
import com.prostamol.Prostamol.domain.port.in.budget.AddBudgetLineUseCase;
import com.prostamol.Prostamol.domain.port.out.BudgetRepositoryPort;
import com.prostamol.Prostamol.domain.port.out.CategoryRepositoryPort;
import org.springframework.security.access.AccessDeniedException;

import java.util.UUID;

public class AddBudgetLineService implements AddBudgetLineUseCase {

    private final BudgetRepositoryPort budgetRepository;
    private final CategoryRepositoryPort categoryRepository;

    public AddBudgetLineService(
        BudgetRepositoryPort budgetRepository,
        CategoryRepositoryPort categoryRepository
    ) {
        this.budgetRepository = budgetRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Budget execute(Command command) {
        Budget budget = budgetRepository
            .findById(command.budgetId())
            .orElseThrow(() -> new IllegalArgumentException("Budget not found: " + command.budgetId()));

        if (!budget.getUserId().equals(command.userId())) {
            throw new AccessDeniedException("Budget does not belong to the authenticated user");
        }

        categoryRepository
            .findById(command.categoryId())
            .filter(category -> category.isSystem() || budget.getUserId().equals(category.getUserId()))
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + command.categoryId()));

        budget.addLine(new BudgetLine(
            UUID.randomUUID(),
            command.categoryId(),
            command.plannedAmount()
        ));

        return budgetRepository.save(budget);
    }
}
