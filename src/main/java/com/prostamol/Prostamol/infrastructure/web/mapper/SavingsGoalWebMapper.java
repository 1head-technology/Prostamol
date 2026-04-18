package com.prostamol.Prostamol.infrastructure.web.mapper;

import com.prostamol.Prostamol.domain.model.savings.SavingsGoal;
import com.prostamol.Prostamol.infrastructure.web.dto.response.SavingsGoalResponse;
import org.springframework.stereotype.Component;

@Component
public class SavingsGoalWebMapper {

    public SavingsGoalResponse toResponse(SavingsGoal goal) {
        return new SavingsGoalResponse(
                goal.getId(), goal.getUserId(), goal.getName(),
                goal.getTargetAmount().amount(), goal.getCurrentAmount().amount(),
                goal.getTargetAmount().currency(),
                goal.getDeadline(), goal.getStatus()
        );
    }
}
