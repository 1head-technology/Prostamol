package com.prostamol.Prostamol.infrastructure.web.dto.response;

import com.prostamol.Prostamol.domain.model.transaction.RecurrenceFrequency;
import com.prostamol.Prostamol.domain.model.transaction.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TransactionResponse(
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
) {}
