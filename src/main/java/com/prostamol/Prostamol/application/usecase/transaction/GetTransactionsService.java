package com.prostamol.Prostamol.application.usecase.transaction;

import com.prostamol.Prostamol.domain.model.transaction.Transaction;
import com.prostamol.Prostamol.domain.port.in.transaction.GetTransactionsUseCase;
import com.prostamol.Prostamol.domain.port.out.AccountRepositoryPort;
import com.prostamol.Prostamol.domain.port.out.TransactionRepositoryPort;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class GetTransactionsService implements GetTransactionsUseCase {

    private final TransactionRepositoryPort transactionRepository;
    private final AccountRepositoryPort accountRepository;

    public GetTransactionsService(
        TransactionRepositoryPort transactionRepository,
        AccountRepositoryPort accountRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public List<Transaction> execute(
        UUID userId,
        UUID accountId,
        LocalDate from,
        LocalDate to
    ) {
        var account = accountRepository.findById(accountId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        if (!account.getUserId().equals(userId)) {
            throw new AccessDeniedException("Account does not belong to the authenticated user");
        }

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
