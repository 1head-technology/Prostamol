package com.prostamol.Prostamol.application.usecase.transaction;

import com.prostamol.Prostamol.domain.model.transaction.Transaction;
import com.prostamol.Prostamol.domain.model.transaction.TransactionType;
import com.prostamol.Prostamol.domain.port.in.transaction.RecordTransactionUseCase;
import com.prostamol.Prostamol.domain.port.out.AccountRepositoryPort;
import com.prostamol.Prostamol.domain.port.out.CategoryRepositoryPort;
import com.prostamol.Prostamol.domain.port.out.TransactionRepositoryPort;

import java.util.UUID;

public class RecordTransactionService implements RecordTransactionUseCase {

    private final TransactionRepositoryPort transactionRepository;
    private final AccountRepositoryPort accountRepository;
    private final CategoryRepositoryPort categoryRepository;

    public RecordTransactionService(
        TransactionRepositoryPort transactionRepository,
        AccountRepositoryPort accountRepository,
        CategoryRepositoryPort categoryRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Transaction execute(Command command) {
        if (
            command.type() == TransactionType.TRANSFER_OUT
            || command.type() == TransactionType.TRANSFER_IN
        ) {
            throw new IllegalArgumentException("Use RecordTransferUseCase for transfer transactions");
        }

        accountRepository
            .findById(command.accountId())
            .orElseThrow(() -> new IllegalArgumentException("Account not found: " + command.accountId()));

        categoryRepository
            .findById(command.categoryId())
            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + command.categoryId()));

        return transactionRepository.save(new Transaction(
            UUID.randomUUID(),
            command.userId(),
            command.accountId(),
            command.type(),
            command.amount(),
            command.date(),
            command.description(),
            command.categoryId(),
            null,
            command.recurring(),
            command.recurrenceFrequency()
        ));
    }
}
