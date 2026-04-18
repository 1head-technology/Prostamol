package com.prostamol.Prostamol.infrastructure.persistence.repository;

import com.prostamol.Prostamol.infrastructure.persistence.entity.TransactionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, UUID> {
    List<TransactionJpaEntity> findAllByAccountId(UUID accountId);
    List<TransactionJpaEntity> findAllByUserId(UUID userId);
    List<TransactionJpaEntity> findAllByAccountIdAndDateBetween(
        UUID accountId,
        LocalDate from,
        LocalDate to
    );
    List<TransactionJpaEntity> findAllByUserIdAndCategoryIdAndDateBetween(
        UUID userId,
        UUID categoryId,
        LocalDate from,
        LocalDate to
    );
}
