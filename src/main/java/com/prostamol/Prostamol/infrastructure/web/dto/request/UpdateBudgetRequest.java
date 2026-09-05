package com.prostamol.Prostamol.infrastructure.web.dto.request;

import com.prostamol.Prostamol.domain.model.budget.BudgetStatus;

import java.time.LocalDate;

public record UpdateBudgetRequest(
    String name,
    LocalDate from,
    LocalDate to,
    BudgetStatus status
) {}
