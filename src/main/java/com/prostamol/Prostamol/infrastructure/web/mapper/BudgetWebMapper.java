package com.prostamol.Prostamol.infrastructure.web.mapper;

import com.prostamol.Prostamol.domain.model.budget.Budget;
import com.prostamol.Prostamol.domain.port.in.budget.GetBudgetSummaryUseCase.BudgetSummary;
import com.prostamol.Prostamol.infrastructure.web.dto.response.BudgetResponse;
import com.prostamol.Prostamol.infrastructure.web.dto.response.BudgetSummaryResponse;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class BudgetWebMapper {

    public BudgetResponse toResponse(Budget budget) {
        var lines = budget.getLines().stream()
                .map(l -> new BudgetResponse.BudgetLineResponse(
                        l.getId(), l.getCategoryId(),
                        l.getPlannedAmount().amount(), l.getPlannedAmount().currency()))
                .collect(Collectors.toList());
        return new BudgetResponse(
                budget.getId(), budget.getUserId(), budget.getName(),
                budget.getPeriod().from(), budget.getPeriod().to(),
                budget.getStatus(), lines
        );
    }

    public BudgetSummaryResponse toSummaryResponse(BudgetSummary summary) {
        var lines = summary.lines().stream()
                .map(l -> new BudgetSummaryResponse.LineSummaryResponse(
                        l.categoryId(), l.categoryName(),
                        l.planned().amount(), l.spent().amount(), l.remaining().amount(),
                        l.planned().currency()))
                .collect(Collectors.toList());
        return new BudgetSummaryResponse(summary.budgetId(), summary.budgetName(), lines);
    }
}
