package com.prostamol.Prostamol.domain.port.in.transaction;

import com.prostamol.Prostamol.domain.model.transaction.RecurrenceFrequency;
import com.prostamol.Prostamol.domain.model.transaction.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public interface UpdateTransactionUseCase {
    Transaction execute(Command command);

    record Command(
        UUID userId,
        UUID transactionId,
        BigDecimal amount,
        String currency,
        LocalDate date,
        String description,
        UUID categoryId,
        Boolean recurring,
        RecurrenceFrequency recurrenceFrequency
    ) {}
}
