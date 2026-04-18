package com.prostamol.Prostamol.domain.port.out;

import com.prostamol.Prostamol.domain.model.savings.SavingsGoal;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SavingsGoalRepositoryPort {
    SavingsGoal save(SavingsGoal savingsGoal);
    Optional<SavingsGoal> findById(UUID id);
    List<SavingsGoal> findAllByUserId(UUID userId);
}
