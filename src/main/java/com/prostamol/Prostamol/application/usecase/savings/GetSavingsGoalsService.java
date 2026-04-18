package com.prostamol.Prostamol.application.usecase.savings;

import com.prostamol.Prostamol.domain.model.savings.SavingsGoal;
import com.prostamol.Prostamol.domain.port.in.savings.GetSavingsGoalsUseCase;
import com.prostamol.Prostamol.domain.port.out.SavingsGoalRepositoryPort;

import java.util.List;
import java.util.UUID;

public class GetSavingsGoalsService implements GetSavingsGoalsUseCase {

    private final SavingsGoalRepositoryPort savingsGoalRepository;

    public GetSavingsGoalsService(SavingsGoalRepositoryPort savingsGoalRepository) {
        this.savingsGoalRepository = savingsGoalRepository;
    }

    @Override
    public List<SavingsGoal> execute(UUID userId) {
        return savingsGoalRepository.findAllByUserId(userId);
    }
}
