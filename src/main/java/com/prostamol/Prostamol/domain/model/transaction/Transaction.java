package com.prostamol.Prostamol.domain.model.transaction;

import com.prostamol.Prostamol.domain.model.shared.Money;

import java.time.LocalDate;
import java.util.UUID;

public class Transaction {

    private final UUID id;
    private final UUID userId;
    private final UUID accountId;
    private final TransactionType type;
    private final Money amount;
    private final LocalDate date;
    private final String description;
    private final UUID categoryId;
    /** Non-null for TRANSFER_OUT / TRANSFER_IN — points to the paired transaction. */
    private final UUID linkedTransactionId;
    private final boolean recurring;
    /** Null when recurring is false. */
    private final RecurrenceFrequency recurrenceFrequency;

    public Transaction(
        UUID id,
        UUID userId,
        UUID accountId,
        TransactionType type,
        Money amount,
        LocalDate date,
        String description,
        UUID categoryId,
        UUID linkedTransactionId,
        boolean recurring,
        RecurrenceFrequency recurrenceFrequency
    ) {
        this.id = id;
        this.userId = userId;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.date = date;
        this.description = description;
        this.categoryId = categoryId;
        this.linkedTransactionId = linkedTransactionId;
        this.recurring = recurring;
        this.recurrenceFrequency = recurrenceFrequency;
    }

    public Transaction update(
        Money amount,
        LocalDate date,
        String description,
        UUID categoryId,
        boolean recurring,
        RecurrenceFrequency recurrenceFrequency
    ) {
        return new Transaction(
            id,
            userId,
            accountId,
            type,
            amount,
            date,
            description,
            categoryId,
            linkedTransactionId,
            recurring,
            recurrenceFrequency
        );
    }

    public UUID getId() {
        return id;
    }
    public UUID getUserId() {
        return userId;
    }
    public UUID getAccountId() {
        return accountId;
    }
    public TransactionType getType() {
        return type;
    }
    public Money getAmount() {
        return amount;
    }
    public LocalDate getDate() {
        return date;
    }
    public String getDescription() {
        return description;
    }
    public UUID getCategoryId() {
        return categoryId;
    }
    public UUID getLinkedTransactionId() {
        return linkedTransactionId;
    }
    public boolean isRecurring() {
        return recurring;
    }
    public RecurrenceFrequency getRecurrenceFrequency() {
        return recurrenceFrequency;
    }
}
