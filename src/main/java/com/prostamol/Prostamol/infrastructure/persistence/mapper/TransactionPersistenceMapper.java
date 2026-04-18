package com.prostamol.Prostamol.infrastructure.persistence.mapper;

import com.prostamol.Prostamol.domain.model.shared.Money;
import com.prostamol.Prostamol.domain.model.transaction.Transaction;
import com.prostamol.Prostamol.infrastructure.persistence.entity.TransactionJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class TransactionPersistenceMapper {

    public Transaction toDomain(TransactionJpaEntity entity) {
        return new Transaction(
            entity.getId(),
            entity.getUserId(),
            entity.getAccountId(),
            entity.getType(),
            Money.of(entity.getAmount(), entity.getCurrency()),
            entity.getDate(),
            entity.getDescription(),
            entity.getCategoryId(),
            entity.getLinkedTransactionId(),
            entity.isRecurring(),
            entity.getRecurrenceFrequency()
        );
    }

    public TransactionJpaEntity toJpaEntity(Transaction transaction) {
        return new TransactionJpaEntity(
            transaction.getId(),
            transaction.getUserId(),
            transaction.getAccountId(),
            transaction.getType(),
            transaction.getAmount().amount(),
            transaction.getAmount().currency(),
            transaction.getDate(),
            transaction.getDescription(),
            transaction.getCategoryId(),
            transaction.getLinkedTransactionId(),
            transaction.isRecurring(),
            transaction.getRecurrenceFrequency()
        );
    }
}
