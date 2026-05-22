package com.prostamol.Prostamol.domain.port.in.account;

import com.prostamol.Prostamol.domain.model.account.Account;
import com.prostamol.Prostamol.domain.model.account.AccountType;

import java.math.BigDecimal;
import java.util.UUID;

public interface UpdateAccountUseCase {
    Account execute(Command command);

    record Command(
        UUID accountId,
        String name,
        AccountType type,
        BigDecimal initialBalance,
        String currency
    ) {}
}
