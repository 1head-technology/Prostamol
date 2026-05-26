package com.prostamol.Prostamol.application.usecase.account;

import com.prostamol.Prostamol.domain.model.transaction.Transaction;
import com.prostamol.Prostamol.domain.port.in.transaction.GetAccountTransactionsUseCase;
import com.prostamol.Prostamol.domain.port.out.TransactionRepositoryPort;

import java.util.List;
import java.util.UUID;

public class GetAccountTransactionsService implements GetAccountTransactionsUseCase {

    private final TransactionRepositoryPort transactionRepository;

    public GetAccountTransactionsService(TransactionRepositoryPort transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Override
    public List<Transaction> execute(UUID accountId) {
        return transactionRepository.findAllByAccountId(accountId);
    }
}
