package com.prostamol.Prostamol.application.usecase.savings;

import com.prostamol.Prostamol.domain.model.savings.SavingsGoal;
import com.prostamol.Prostamol.domain.model.savings.SavingsGoalStatus;
import com.prostamol.Prostamol.domain.model.shared.Money;
import com.prostamol.Prostamol.domain.port.in.savings.CreateSavingsGoalUseCase;
import com.prostamol.Prostamol.domain.port.out.SavingsGoalRepositoryPort;

import java.util.UUID;

public class CreateSavingsGoalService implements CreateSavingsGoalUseCase {

    private final SavingsGoalRepositoryPort savingsGoalRepository;

    public CreateSavingsGoalService(SavingsGoalRepositoryPort savingsGoalRepository) {
        this.savingsGoalRepository = savingsGoalRepository;
    }

    @Override
    public SavingsGoal execute(Command command) {
        Money initialAmount = Money.zero(command.targetAmount().currency());

        return savingsGoalRepository.save(new SavingsGoal(
            UUID.randomUUID(),
            command.userId(),
            command.name(),
            command.targetAmount(),
            initialAmount,
            command.deadline(),
            SavingsGoalStatus.ACTIVE
        ));
    }
}
