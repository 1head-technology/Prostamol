package com.prostamol.Prostamol.application.usecase.budget;

import com.prostamol.Prostamol.domain.model.budget.Budget;
import com.prostamol.Prostamol.domain.model.budget.BudgetLine;
import com.prostamol.Prostamol.domain.model.category.Category;
import com.prostamol.Prostamol.domain.model.shared.Money;
import com.prostamol.Prostamol.domain.model.transaction.Transaction;
import com.prostamol.Prostamol.domain.model.transaction.TransactionType;
import com.prostamol.Prostamol.domain.port.in.budget.GetBudgetSummaryUseCase;
import com.prostamol.Prostamol.domain.port.out.BudgetRepositoryPort;
import com.prostamol.Prostamol.domain.port.out.CategoryRepositoryPort;
import com.prostamol.Prostamol.domain.port.out.TransactionRepositoryPort;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GetBudgetSummaryService implements GetBudgetSummaryUseCase {

    private final BudgetRepositoryPort budgetRepository;
    private final TransactionRepositoryPort transactionRepository;
    private final CategoryRepositoryPort categoryRepository;

    public GetBudgetSummaryService(
        BudgetRepositoryPort budgetRepository,
        TransactionRepositoryPort transactionRepository,
        CategoryRepositoryPort categoryRepository
    ) {
        this.budgetRepository = budgetRepository;
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public BudgetSummary execute(UUID budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
            .orElseThrow(() -> new IllegalArgumentException("Budget not found: " + budgetId));

        List<LineSummary> lines = new ArrayList<>();

        for (BudgetLine line : budget.getLines()) {
            Category category = categoryRepository.findById(line.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + line.getCategoryId()));

            List<Transaction> transactions = transactionRepository.findAllByUserIdAndCategoryIdAndDateBetween(
                budget.getUserId(),
                line.getCategoryId(),
                budget.getPeriod().from(),
                budget.getPeriod().to()
            );

            String currency = line.getPlannedAmount().currency();
            BigDecimal spent = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE)
                .map(t -> t.getAmount().amount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

            Money spentMoney = Money.of(spent, currency);
            Money remaining = line.getPlannedAmount().subtract(spentMoney);
            lines.add(new LineSummary(
                line.getCategoryId(),
                category.getName(),
                line.getPlannedAmount(),
                spentMoney,
                remaining
            ));
        }

        return new BudgetSummary(budget.getId(), budget.getName(), lines);
    }
}
