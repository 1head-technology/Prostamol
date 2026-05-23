package com.prostamol.Prostamol.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RecordTransferRequest(
    @NotNull UUID sourceAccountId,
    @NotNull UUID destinationAccountId,
    @NotNull @Positive BigDecimal amount,
    @NotNull @Size(min = 3, max = 3) String currency,
    @NotNull LocalDate date,
    String description
) {}
