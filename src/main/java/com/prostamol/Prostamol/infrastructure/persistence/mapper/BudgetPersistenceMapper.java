package com.prostamol.Prostamol.infrastructure.persistence.mapper;

import com.prostamol.Prostamol.domain.model.budget.Budget;
import com.prostamol.Prostamol.domain.model.budget.BudgetLine;
import com.prostamol.Prostamol.domain.model.shared.DateRange;
import com.prostamol.Prostamol.domain.model.shared.Money;
import com.prostamol.Prostamol.infrastructure.persistence.entity.BudgetJpaEntity;
import com.prostamol.Prostamol.infrastructure.persistence.entity.BudgetLineJpaEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BudgetPersistenceMapper {

    public Budget toDomain(BudgetJpaEntity entity) {
        List<BudgetLine> lines = entity.getLines().stream()
            .map(l -> new BudgetLine(
                l.getId(),
                l.getCategoryId(),
                Money.of(l.getPlannedAmount(),
                l.getCurrency()))
            )
            .collect(Collectors.toList());

        return new Budget(
            entity.getId(),
            entity.getUserId(),
            entity.getName(),
            new DateRange(entity.getPeriodFrom(),
            entity.getPeriodTo()),
            entity.getStatus(),
            lines
        );
    }

    public BudgetJpaEntity toJpaEntity(Budget budget) {
        List<BudgetLineJpaEntity> lines = budget.getLines().stream()
            .map(l -> new BudgetLineJpaEntity(
                l.getId(),
                l.getCategoryId(),
                l.getPlannedAmount().amount(),
                l.getPlannedAmount().currency())
            )
            .collect(Collectors.toList());

        return new BudgetJpaEntity(
            budget.getId(),
            budget.getUserId(),
            budget.getName(),
            budget.getPeriod().from(),
            budget.getPeriod().to(),
            budget.getStatus(),
            lines
        );
    }
}
