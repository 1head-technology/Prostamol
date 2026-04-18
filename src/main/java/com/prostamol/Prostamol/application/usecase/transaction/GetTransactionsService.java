package com.prostamol.Prostamol.application.usecase.transaction;

import com.prostamol.Prostamol.domain.model.transaction.Transaction;
import com.prostamol.Prostamol.domain.port.in.transaction.GetTransactionsUseCase;
import com.prostamol.Prostamol.domain.port.out.TransactionRepositoryPort;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class GetTransactionsService implements GetTransactionsUseCase {

    private final TransactionRepositoryPort transactionRepository;

    public GetTransactionsService(TransactionRepositoryPort transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public List<Transaction> execute(
        UUID accountId,
        LocalDate from,
        LocalDate to
    ) {
        if (from != null && to != null) {
            return transactionRepository.findAllByAccountIdAndDateBetween(accountId, from, to);
        }

        return transactionRepository.findAllByAccountId(accountId);
    }

    @Override
    public List<Transaction> execute(UUID userId) {
        return transactionRepository.findAllByUserId(userId);
    }
}
