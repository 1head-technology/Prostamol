package com.prostamol.Prostamol.domain.model.budget;

import com.prostamol.Prostamol.domain.model.shared.Money;

import java.util.UUID;

public class BudgetLine {

    private final UUID id;
    private final UUID categoryId;
    private final Money plannedAmount;

    public BudgetLine(
        UUID id,
        UUID categoryId,
        Money plannedAmount
    ) {
        this.id = id;
        this.categoryId = categoryId;
        this.plannedAmount = plannedAmount;
    }

    public UUID getId() {
        return id;
    }
    public UUID getCategoryId() {
        return categoryId;
    }
    public Money getPlannedAmount() {
        return plannedAmount;
    }
}
