package com.prostamol.Prostamol.domain.port.in.transaction;

import com.prostamol.Prostamol.domain.model.transaction.Transaction;

import java.util.List;
import java.util.UUID;

public interface GetAccountTransactionsUseCase {
    List<Transaction> execute(UUID accountId);
}
