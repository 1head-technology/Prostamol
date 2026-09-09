package com.prostamol.Prostamol.domain.model.budget;

import com.prostamol.Prostamol.domain.model.shared.DateRange;
import com.prostamol.Prostamol.domain.model.shared.Money;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Budget {

    private final UUID id;
    private final UUID userId;
    private final String name;
    private final DateRange period;
    private BudgetStatus status;
    private final List<BudgetLine> lines;

    public Budget(
        UUID id,
        UUID userId,
        String name,
        DateRange period,
        BudgetStatus status,
        List<BudgetLine> lines
    ) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.period = period;
        this.status = status;
        this.lines = new ArrayList<>(lines);
        assertCurrencyCoherence(this.lines);
    }

    public Budget update(String name, DateRange period, BudgetStatus status) {
        return new Budget(id, userId, name, period, status, lines);
    }

    public void addLine(BudgetLine line) {
        if (!lines.isEmpty()) {
            lines.getFirst().getPlannedAmount().assertSameCurrency(line.getPlannedAmount());
        }
        lines.add(line);
    }

    public void updateLine(UUID lineId, UUID categoryId, Money plannedAmount) {
        for (int i = 0; i < lines.size(); i++) {
            if (lines.get(i).getId().equals(lineId)) {
                BudgetLine updatedLine = new BudgetLine(lineId, categoryId, plannedAmount);
                for (BudgetLine line : lines) {
                    if (!line.getId().equals(lineId)) {
                        line.getPlannedAmount().assertSameCurrency(updatedLine.getPlannedAmount());
                    }
                }
                lines.set(i, updatedLine);
                return;
            }
        }
        throw new IllegalArgumentException("Budget line not found: " + lineId);
    }

    public void close() {
        this.status = BudgetStatus.CLOSED;
    }

    public UUID getId() {
        return id;
    }
    public UUID getUserId() {
        return userId;
    }
    public String getName() {
        return name;
    }
    public DateRange getPeriod() {
        return period;
    }
    public BudgetStatus getStatus() {
        return status;
    }
    public List<BudgetLine> getLines() {
        return Collections.unmodifiableList(lines);
    }

    private static void assertCurrencyCoherence(List<BudgetLine> lines) {
        if (lines.isEmpty()) {
            return;
        }
        Money budgetCurrency = lines.getFirst().getPlannedAmount();
        lines.forEach(line -> budgetCurrency.assertSameCurrency(line.getPlannedAmount()));
    }
}
