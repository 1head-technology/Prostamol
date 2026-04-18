package com.prostamol.Prostamol.domain.model.savings;

import com.prostamol.Prostamol.domain.model.shared.Money;

import java.time.LocalDate;
import java.util.UUID;

public class SavingsGoal {

    private final UUID id;
    private final UUID userId;
    private final String name;
    private final Money targetAmount;
    private Money currentAmount;
    private final LocalDate deadline;
    private SavingsGoalStatus status;

    public SavingsGoal(
        UUID id,
        UUID userId,
        String name,
        Money targetAmount,
        Money currentAmount,
        LocalDate deadline,
        SavingsGoalStatus status
    ) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.deadline = deadline;
        this.status = status;
    }

    public void addContribution(Money contribution) {
        this.currentAmount = this.currentAmount.add(contribution);

        if (this.currentAmount.amount().compareTo(this.targetAmount.amount()) >= 0) {
            this.status = SavingsGoalStatus.ACHIEVED;
        }
    }

    public void cancel() {
        this.status = SavingsGoalStatus.CANCELLED;
    }

    public UUID getId() {
        return id;
    }
    public UUID getUserId() {
        return userId;
    }
    public String getName() {
        return name;
    }
    public Money getTargetAmount() {
        return targetAmount;
    }
    public Money getCurrentAmount() {
        return currentAmount;
    }
    public LocalDate getDeadline() {
        return deadline;
    }
    public SavingsGoalStatus getStatus() {
        return status;
    }
}
