package com.prostamol.Prostamol.application.usecase.transaction;

import com.prostamol.Prostamol.domain.model.shared.Money;
import com.prostamol.Prostamol.domain.model.transaction.RecurrenceFrequency;
import com.prostamol.Prostamol.domain.model.transaction.Transaction;
import com.prostamol.Prostamol.domain.model.transaction.TransactionType;
import com.prostamol.Prostamol.domain.port.in.transaction.UpdateTransactionUseCase;
import com.prostamol.Prostamol.domain.port.out.CategoryRepositoryPort;
import com.prostamol.Prostamol.domain.port.out.TransactionRepositoryPort;

import java.util.UUID;

public class UpdateTransactionService implements UpdateTransactionUseCase {

    private final TransactionRepositoryPort transactionRepository;
    private final CategoryRepositoryPort categoryRepository;

    public UpdateTransactionService(
        TransactionRepositoryPort transactionRepository,
        CategoryRepositoryPort categoryRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Transaction execute(Command command) {
        Transaction transaction = transactionRepository
            .findById(command.transactionId())
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + command.transactionId()));

        if (!transaction.getUserId().equals(command.userId())) {
            throw new IllegalArgumentException("Transaction does not belong to user: " + command.userId());
        }

        if (
            transaction.getType() == TransactionType.TRANSFER_OUT
            || transaction.getType() == TransactionType.TRANSFER_IN
        ) {
            throw new IllegalArgumentException("Transfer transactions cannot be updated");
        }

        Money currentAmount = transaction.getAmount();
        Money updatedAmount = Money.of(
            command.amount() != null ? command.amount() : currentAmount.amount(),
            command.currency() != null ? command.currency() : currentAmount.currency()
        );

        UUID updatedCategoryId = command.categoryId() != null ? command.categoryId() : transaction.getCategoryId();
        if (command.categoryId() != null) {
            categoryRepository
                .findById(command.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + command.categoryId()));
        }

        boolean updatedRecurring = command.recurring() != null ? command.recurring() : transaction.isRecurring();
        RecurrenceFrequency updatedFrequency = command.recurrenceFrequency() != null
            ? command.recurrenceFrequency()
            : transaction.getRecurrenceFrequency();

        if (updatedRecurring && updatedFrequency == null) {
            throw new IllegalArgumentException("recurrenceFrequency is required when recurring is true");
        }
        if (!updatedRecurring) {
            updatedFrequency = null;
        }

        return transactionRepository.save(transaction.update(
            updatedAmount,
            command.date() != null ? command.date() : transaction.getDate(),
            command.description() != null ? command.description() : transaction.getDescription(),
            updatedCategoryId,
            updatedRecurring,
            updatedFrequency
        ));
    }
}
