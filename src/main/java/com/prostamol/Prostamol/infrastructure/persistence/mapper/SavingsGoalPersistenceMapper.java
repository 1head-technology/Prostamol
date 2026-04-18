package com.prostamol.Prostamol.infrastructure.persistence.mapper;

import com.prostamol.Prostamol.domain.model.savings.SavingsGoal;
import com.prostamol.Prostamol.domain.model.shared.Money;
import com.prostamol.Prostamol.infrastructure.persistence.entity.SavingsGoalJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class SavingsGoalPersistenceMapper {

    public SavingsGoal toDomain(SavingsGoalJpaEntity entity) {
        return new SavingsGoal(
            entity.getId(),
            entity.getUserId(),
            entity.getName(),
            Money.of(entity.getTargetAmount(), entity.getCurrency()),
            Money.of(entity.getCurrentAmount(), entity.getCurrency()),
            entity.getDeadline(),
            entity.getStatus()
        );
    }

    public SavingsGoalJpaEntity toJpaEntity(SavingsGoal goal) {
        return new SavingsGoalJpaEntity(
            goal.getId(),
            goal.getUserId(),
            goal.getName(),
            goal.getTargetAmount().amount(),
            goal.getCurrentAmount().amount(),
            goal.getTargetAmount().currency(),
            goal.getDeadline(),
            goal.getStatus()
        );
    }
}
