package com.prostamol.Prostamol.domain.port.in.savings;

import com.prostamol.Prostamol.domain.model.savings.SavingsGoal;
import com.prostamol.Prostamol.domain.model.shared.Money;

import java.time.LocalDate;
import java.util.UUID;

public interface CreateSavingsGoalUseCase {
    SavingsGoal execute(Command command);

    record Command(UUID userId, String name, Money targetAmount, LocalDate deadline) {}
}
