package com.prostamol.Prostamol.application.usecase.savings;

import com.prostamol.Prostamol.domain.model.savings.SavingsGoal;
import com.prostamol.Prostamol.domain.model.shared.Money;
import com.prostamol.Prostamol.domain.port.in.savings.AddSavingsContributionUseCase;
import com.prostamol.Prostamol.domain.port.out.SavingsGoalRepositoryPort;

import java.util.UUID;

public class AddSavingsContributionService implements AddSavingsContributionUseCase {

    private final SavingsGoalRepositoryPort savingsGoalRepository;

    public AddSavingsContributionService(SavingsGoalRepositoryPort savingsGoalRepository) {
        this.savingsGoalRepository = savingsGoalRepository;
    }

    @Override
    public SavingsGoal execute(UUID savingsGoalId, Money amount) {
        SavingsGoal goal = savingsGoalRepository
            .findById(savingsGoalId)
            .orElseThrow(() -> new IllegalArgumentException("Savings goal not found: " + savingsGoalId));

        goal.addContribution(amount);

        return savingsGoalRepository.save(goal);
    }
}
