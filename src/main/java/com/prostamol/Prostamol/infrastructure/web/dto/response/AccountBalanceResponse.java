package com.prostamol.Prostamol.infrastructure.web.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountBalanceResponse(UUID accountId, BigDecimal balance, String currency) {}
