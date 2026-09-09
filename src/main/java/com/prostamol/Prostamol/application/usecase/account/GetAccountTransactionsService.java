package com.prostamol.Prostamol.application.usecase.account;

import com.prostamol.Prostamol.domain.model.transaction.Transaction;
import com.prostamol.Prostamol.domain.port.in.transaction.GetAccountTransactionsUseCase;
import com.prostamol.Prostamol.domain.port.out.AccountRepositoryPort;
import com.prostamol.Prostamol.domain.port.out.TransactionRepositoryPort;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;

public class GetAccountTransactionsService implements GetAccountTransactionsUseCase {

    private final TransactionRepositoryPort transactionRepository;
    private final AccountRepositoryPort accountRepository;

    public GetAccountTransactionsService(
        TransactionRepositoryPort transactionRepository,
        AccountRepositoryPort accountRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public List<Transaction> execute(UUID userId, UUID accountId) {
        var account = accountRepository.findById(accountId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        if (!account.getUserId().equals(userId)) {
            throw new AccessDeniedException("Account does not belong to the authenticated user");
        }

        return transactionRepository.findAllByAccountId(accountId);
    }
}
