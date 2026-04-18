package com.prostamol.Prostamol.domain.port.in.account;

import com.prostamol.Prostamol.domain.model.account.Account;
import com.prostamol.Prostamol.domain.model.account.AccountType;
import com.prostamol.Prostamol.domain.model.shared.Money;

import java.util.UUID;

public interface CreateAccountUseCase {
    Account execute(Command command);

    record Command(
        UUID userId,
        String name,
        AccountType type,
        Money initialBalance
    ) {}
}
