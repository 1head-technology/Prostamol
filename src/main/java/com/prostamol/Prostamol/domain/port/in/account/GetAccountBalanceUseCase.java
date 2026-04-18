package com.prostamol.Prostamol.domain.port.in.account;

import com.prostamol.Prostamol.domain.model.shared.Money;

import java.util.UUID;

public interface GetAccountBalanceUseCase {
    Money execute(UUID accountId);
}
