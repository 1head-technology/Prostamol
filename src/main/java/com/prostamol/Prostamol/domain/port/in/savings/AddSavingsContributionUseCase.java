package com.prostamol.Prostamol.domain.port.in.savings;

import com.prostamol.Prostamol.domain.model.savings.SavingsGoal;
import com.prostamol.Prostamol.domain.model.shared.Money;

import java.util.UUID;

public interface AddSavingsContributionUseCase {
    SavingsGoal execute(UUID savingsGoalId, Money amount);
}
