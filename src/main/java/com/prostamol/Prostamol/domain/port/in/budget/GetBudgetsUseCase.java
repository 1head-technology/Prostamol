package com.prostamol.Prostamol.domain.port.in.budget;

import com.prostamol.Prostamol.domain.model.budget.Budget;

import java.util.List;
import java.util.UUID;

public interface GetBudgetsUseCase {
    List<Budget> execute(UUID userId);
}
