package com.prostamol.Prostamol.infrastructure.persistence.repository;

import com.prostamol.Prostamol.infrastructure.persistence.entity.BudgetJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BudgetJpaRepository extends JpaRepository<BudgetJpaEntity, UUID> {
    List<BudgetJpaEntity> findAllByUserId(UUID userId);
}
