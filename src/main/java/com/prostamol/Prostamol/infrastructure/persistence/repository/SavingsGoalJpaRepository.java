package com.prostamol.Prostamol.infrastructure.persistence.repository;

import com.prostamol.Prostamol.infrastructure.persistence.entity.SavingsGoalJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SavingsGoalJpaRepository extends JpaRepository<SavingsGoalJpaEntity, UUID> {
    List<SavingsGoalJpaEntity> findAllByUserId(UUID userId);
}
