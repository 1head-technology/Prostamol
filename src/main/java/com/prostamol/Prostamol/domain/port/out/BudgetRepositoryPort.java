package com.prostamol.Prostamol.domain.port.out;

import com.prostamol.Prostamol.domain.model.budget.Budget;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepositoryPort {
    Budget save(Budget budget);
    Optional<Budget> findById(UUID id);
    List<Budget> findAllByUserId(UUID userId);
}
