package com.prostamol.Prostamol.domain.port.in.savings;

import com.prostamol.Prostamol.domain.model.savings.SavingsGoal;

import java.util.List;
import java.util.UUID;

public interface GetSavingsGoalsUseCase {
    List<SavingsGoal> execute(UUID userId);
}
