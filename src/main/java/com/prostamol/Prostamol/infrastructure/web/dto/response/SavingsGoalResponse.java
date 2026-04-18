package com.prostamol.Prostamol.infrastructure.web.dto.response;

import com.prostamol.Prostamol.domain.model.savings.SavingsGoalStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record SavingsGoalResponse(
    UUID id,
    UUID userId,
    String name,
    BigDecimal targetAmount,
    BigDecimal currentAmount,
    String currency,
    LocalDate deadline,
    SavingsGoalStatus status
) {}
