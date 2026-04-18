package com.prostamol.Prostamol.infrastructure.web.dto.request;

import com.prostamol.Prostamol.domain.model.transaction.RecurrenceFrequency;
import com.prostamol.Prostamol.domain.model.transaction.TransactionType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RecordTransactionRequest(
    @NotNull UUID accountId,
    @NotNull TransactionType type,
    @NotNull @Positive BigDecimal amount,
    @NotNull @Size(min = 3, max = 3) String currency,
    @NotNull LocalDate date,
    String description,
    @NotNull UUID categoryId,
    boolean recurring,
    RecurrenceFrequency recurrenceFrequency  // required when recurring = true
) {}
