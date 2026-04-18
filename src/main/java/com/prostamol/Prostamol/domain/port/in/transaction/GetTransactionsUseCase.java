package com.prostamol.Prostamol.domain.port.in.transaction;

import com.prostamol.Prostamol.domain.model.transaction.Transaction;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface GetTransactionsUseCase {
    List<Transaction> execute(UUID accountId, LocalDate from, LocalDate to);
    List<Transaction> execute(UUID userId);
}
