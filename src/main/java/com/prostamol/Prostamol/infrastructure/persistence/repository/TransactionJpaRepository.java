package com.prostamol.Prostamol.infrastructure.persistence.repository;

import com.prostamol.Prostamol.infrastructure.persistence.entity.TransactionJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TransactionJpaRepository extends JpaRepository<TransactionJpaEntity, UUID> {

    List<TransactionJpaEntity> findAllByAccountIdOrderByDateDescCreatedAtDesc(UUID accountId);

    List<TransactionJpaEntity> findAllByUserIdOrderByDateDescCreatedAtDesc(UUID userId);

    List<TransactionJpaEntity> findAllByAccountIdAndDateBetweenOrderByDateDescCreatedAtDesc(
        UUID accountId,
        LocalDate from,
        LocalDate to
    );

    List<TransactionJpaEntity> findAllByUserIdAndCategoryIdAndDateBetweenOrderByDateDescCreatedAtDesc(
        UUID userId,
        UUID categoryId,
        LocalDate from,
        LocalDate to
    );
}
