package com.prostamol.Prostamol.domain.port.in.transaction;

import com.prostamol.Prostamol.domain.model.shared.Money;
import com.prostamol.Prostamol.domain.model.transaction.RecurrenceFrequency;
import com.prostamol.Prostamol.domain.model.transaction.Transaction;
import com.prostamol.Prostamol.domain.model.transaction.TransactionType;

import java.time.LocalDate;
import java.util.UUID;

public interface RecordTransactionUseCase {
    Transaction execute(Command command);

    record Command(
        UUID userId,
        UUID accountId,
        TransactionType type,       // INCOME or EXPENSE only
        Money amount,
        LocalDate date,
        String description,
        UUID categoryId,
        boolean recurring,
        RecurrenceFrequency recurrenceFrequency  // null when recurring = false
    ) {}
}
