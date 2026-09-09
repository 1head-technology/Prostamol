package com.prostamol.Prostamol.application.usecase.transaction;

import com.prostamol.Prostamol.domain.model.shared.Money;
import com.prostamol.Prostamol.domain.model.transaction.RecurrenceFrequency;
import com.prostamol.Prostamol.domain.model.transaction.Transaction;
import com.prostamol.Prostamol.domain.model.transaction.TransactionType;
import com.prostamol.Prostamol.domain.port.in.transaction.UpdateTransactionUseCase;
import com.prostamol.Prostamol.domain.port.out.AccountRepositoryPort;
import com.prostamol.Prostamol.domain.port.out.CategoryRepositoryPort;
import com.prostamol.Prostamol.domain.port.out.TransactionRepositoryPort;
import org.springframework.security.access.AccessDeniedException;

import java.util.UUID;

public class UpdateTransactionService implements UpdateTransactionUseCase {

    private final TransactionRepositoryPort transactionRepository;
    private final CategoryRepositoryPort categoryRepository;
    private final AccountRepositoryPort accountRepository;

    public UpdateTransactionService(
        TransactionRepositoryPort transactionRepository,
        CategoryRepositoryPort categoryRepository,
        AccountRepositoryPort accountRepository
    ) {
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.accountRepository = accountRepository;
    }

    @Override
    public Transaction execute(Command command) {
        Transaction transaction = transactionRepository
            .findById(command.transactionId())
            .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + command.transactionId()));

        if (!transaction.getUserId().equals(command.userId())) {
            throw new AccessDeniedException("Transaction does not belong to the authenticated user");
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
        var account = accountRepository
            .findById(transaction.getAccountId())
            .orElseThrow(() -> new IllegalArgumentException("Account not found: " + transaction.getAccountId()));
        if (!account.getUserId().equals(command.userId())) {
            throw new AccessDeniedException("Account does not belong to the authenticated user");
        }
        account.getInitialBalance().assertSameCurrency(updatedAmount);

        UUID updatedCategoryId = command.categoryId() != null ? command.categoryId() : transaction.getCategoryId();
        if (command.categoryId() != null) {
            var category = categoryRepository
                .findById(command.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + command.categoryId()));

            if (!category.isSystem() && !command.userId().equals(category.getUserId())) {
                throw new AccessDeniedException("Category does not belong to the authenticated user");
            }
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
