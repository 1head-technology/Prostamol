package com.prostamol.Prostamol.application.usecase.budget;

import com.prostamol.Prostamol.domain.model.budget.*;
import com.prostamol.Prostamol.domain.model.category.*;
import com.prostamol.Prostamol.domain.model.shared.*;
import com.prostamol.Prostamol.domain.port.in.budget.*;
import com.prostamol.Prostamol.domain.port.out.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class BudgetCategoryOwnershipTests {
    enum Operation { CREATE, ADD, UPDATE }

    private final BudgetRepositoryPort budgets = mock(BudgetRepositoryPort.class);
    private final CategoryRepositoryPort categories = mock(CategoryRepositoryPort.class);
    private final UUID userId = UUID.randomUUID();
    private final UUID categoryId = UUID.randomUUID();
    private final Money amount = Money.of(BigDecimal.TEN, "EUR");
    private final BudgetLine line = new BudgetLine(UUID.randomUUID(), UUID.randomUUID(), amount);
    private final Budget budget = new Budget(UUID.randomUUID(), userId, "Budget",
        new DateRange(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)),
        BudgetStatus.ACTIVE, List.of(line));

    @ParameterizedTest
    @EnumSource(Operation.class)
    void rejectsAnotherUsersCategoryWithoutSaving(Operation operation) {
        category(UUID.randomUUID(), false);
        assertThrows(IllegalArgumentException.class, () -> execute(operation));
        verify(budgets, never()).save(any());
        assertEquals(List.of(line), budget.getLines());
    }

    @ParameterizedTest
    @EnumSource(Operation.class)
    void acceptsBudgetOwnersCategory(Operation operation) {
        category(userId, false);
        assertCategorySaved(execute(operation));
    }

    @ParameterizedTest
    @EnumSource(Operation.class)
    void acceptsSystemCategory(Operation operation) {
        category(null, true);
        assertCategorySaved(execute(operation));
    }

    @ParameterizedTest
    @EnumSource(Operation.class)
    void rejectsMissingCategoryWithoutSaving(Operation operation) {
        when(categories.findById(categoryId)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> execute(operation));
        verify(budgets, never()).save(any());
    }

    private void category(UUID owner, boolean system) {
        when(categories.findById(categoryId)).thenReturn(Optional.of(
            new Category(categoryId, owner, "Category", CategoryType.EXPENSE, system)));
    }

    private Budget execute(Operation operation) {
        when(budgets.findById(budget.getId())).thenReturn(Optional.of(budget));
        when(budgets.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        return switch (operation) {
            case CREATE -> new CreateBudgetService(budgets, categories).execute(
                new CreateBudgetUseCase.Command(userId, "Budget", budget.getPeriod(),
                    List.of(new CreateBudgetUseCase.LineCommand(categoryId, amount))));
            case ADD -> new AddBudgetLineService(budgets, categories).execute(
                new AddBudgetLineUseCase.Command(userId, budget.getId(), categoryId, amount));
            case UPDATE -> new UpdateBudgetLineService(budgets, categories).execute(
                new UpdateBudgetLineUseCase.Command(userId, budget.getId(), line.getId(), categoryId, null, null));
        };
    }

    private void assertCategorySaved(Budget result) {
        assertTrue(result.getLines().stream().anyMatch(l -> l.getCategoryId().equals(categoryId)));
        verify(budgets).save(any());
    }
}
