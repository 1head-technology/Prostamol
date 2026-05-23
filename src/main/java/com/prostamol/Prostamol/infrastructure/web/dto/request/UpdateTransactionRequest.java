package com.prostamol.Prostamol.infrastructure.web.dto.request;

import com.prostamol.Prostamol.domain.model.transaction.RecurrenceFrequency;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record UpdateTransactionRequest(
    @Positive BigDecimal amount,
    @Size(min = 3, max = 3) String currency,
    LocalDate date,
    String description,
    UUID categoryId,
    Boolean recurring,
    RecurrenceFrequency recurrenceFrequency
) {}
