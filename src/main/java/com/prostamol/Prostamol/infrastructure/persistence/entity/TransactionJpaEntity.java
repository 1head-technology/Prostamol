package com.prostamol.Prostamol.infrastructure.persistence.entity;

import com.prostamol.Prostamol.domain.model.transaction.RecurrenceFrequency;
import com.prostamol.Prostamol.domain.model.transaction.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class TransactionJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private LocalDate date;

    @Column
    private String description;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "linked_transaction_id")
    private UUID linkedTransactionId;

    @Column(nullable = false)
    private boolean recurring;

    @Enumerated(EnumType.STRING)
    @Column(name = "recurrence_frequency")
    private RecurrenceFrequency recurrenceFrequency;

    protected TransactionJpaEntity() {}

    public TransactionJpaEntity(
        UUID id,
        UUID userId,
        UUID accountId,
        TransactionType type,
        BigDecimal amount,
        String currency,
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
        this.currency = currency;
        this.date = date;
        this.description = description;
        this.categoryId = categoryId;
        this.linkedTransactionId = linkedTransactionId;
        this.recurring = recurring;
        this.recurrenceFrequency = recurrenceFrequency;
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
    public BigDecimal getAmount() {
        return amount;
    }
    public String getCurrency() {
        return currency;
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
