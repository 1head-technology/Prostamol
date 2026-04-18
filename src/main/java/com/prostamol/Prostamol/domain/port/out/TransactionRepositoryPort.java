package com.prostamol.Prostamol.domain.port.out;

import com.prostamol.Prostamol.domain.model.transaction.Transaction;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface TransactionRepositoryPort {
    Transaction save(Transaction transaction);
    List<Transaction> saveAll(List<Transaction> transactions);
    List<Transaction> findAllByAccountId(UUID accountId);
    List<Transaction> findAllByUserId(UUID userId);
    List<Transaction> findAllByAccountIdAndDateBetween(
        UUID accountId,
        LocalDate from,
        LocalDate to
    );
    List<Transaction> findAllByUserIdAndCategoryIdAndDateBetween(
        UUID userId,
        UUID categoryId,
        LocalDate from,
        LocalDate to
    );
}
