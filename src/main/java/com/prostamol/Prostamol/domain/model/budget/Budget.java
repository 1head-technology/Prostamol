package com.prostamol.Prostamol.domain.model.budget;

import com.prostamol.Prostamol.domain.model.shared.DateRange;

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
    }

    public void addLine(BudgetLine line) {
        lines.add(line);
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
}
