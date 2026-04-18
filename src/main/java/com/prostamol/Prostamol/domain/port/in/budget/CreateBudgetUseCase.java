package com.prostamol.Prostamol.domain.port.in.budget;

import com.prostamol.Prostamol.domain.model.budget.Budget;
import com.prostamol.Prostamol.domain.model.shared.DateRange;
import com.prostamol.Prostamol.domain.model.shared.Money;

import java.util.List;
import java.util.UUID;

public interface CreateBudgetUseCase {
    Budget execute(Command command);

    record Command(UUID userId, String name, DateRange period, List<LineCommand> lines) {}
    record LineCommand(UUID categoryId, Money plannedAmount) {}
}
