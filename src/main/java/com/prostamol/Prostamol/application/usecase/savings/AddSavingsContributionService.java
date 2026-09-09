package com.prostamol.Prostamol.application.usecase.savings;

import com.prostamol.Prostamol.domain.model.savings.SavingsGoal;
import com.prostamol.Prostamol.domain.model.shared.Money;
import com.prostamol.Prostamol.domain.port.in.savings.AddSavingsContributionUseCase;
import com.prostamol.Prostamol.domain.port.out.SavingsGoalRepositoryPort;
import org.springframework.security.access.AccessDeniedException;

import java.util.UUID;

public class AddSavingsContributionService implements AddSavingsContributionUseCase {

    private final SavingsGoalRepositoryPort savingsGoalRepository;

    public AddSavingsContributionService(SavingsGoalRepositoryPort savingsGoalRepository) {
        this.savingsGoalRepository = savingsGoalRepository;
    }

    @Override
    public SavingsGoal execute(UUID userId, UUID savingsGoalId, Money amount) {
        SavingsGoal goal = savingsGoalRepository
            .findById(savingsGoalId)
            .orElseThrow(() -> new IllegalArgumentException("Savings goal not found: " + savingsGoalId));

        if (!goal.getUserId().equals(userId)) {
            throw new AccessDeniedException("Savings goal does not belong to the authenticated user");
        }

        goal.addContribution(amount);

        return savingsGoalRepository.save(goal);
    }
}
