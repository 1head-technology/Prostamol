package com.prostamol.Prostamol.application.usecase.budget;

import com.prostamol.Prostamol.domain.model.budget.Budget;
import com.prostamol.Prostamol.domain.model.budget.BudgetLine;
import com.prostamol.Prostamol.domain.model.shared.Money;
import com.prostamol.Prostamol.domain.port.in.budget.UpdateBudgetLineUseCase;
import com.prostamol.Prostamol.domain.port.out.BudgetRepositoryPort;
import com.prostamol.Prostamol.domain.port.out.CategoryRepositoryPort;
import org.springframework.security.access.AccessDeniedException;

import java.util.UUID;

public class UpdateBudgetLineService implements UpdateBudgetLineUseCase {

    private final BudgetRepositoryPort budgetRepository;
    private final CategoryRepositoryPort categoryRepository;

    public UpdateBudgetLineService(
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

        BudgetLine current = budget.getLines().stream()
            .filter(line -> line.getId().equals(command.lineId()))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Budget line not found: " + command.lineId()));

        if (command.categoryId() != null) {
            categoryRepository
                .findById(command.categoryId())
                .filter(category -> category.isSystem() || budget.getUserId().equals(category.getUserId()))
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + command.categoryId()));
        }

        Money currentAmount = current.getPlannedAmount();
        Money updatedAmount = Money.of(
            command.plannedAmount() != null ? command.plannedAmount() : currentAmount.amount(),
            command.currency() != null ? command.currency() : currentAmount.currency()
        );

        budget.updateLine(
            command.lineId(),
            command.categoryId() != null ? command.categoryId() : current.getCategoryId(),
            updatedAmount
        );

        return budgetRepository.save(budget);
    }
}
