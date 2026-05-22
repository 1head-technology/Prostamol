package com.prostamol.Prostamol.domain.port.in.account;

import com.prostamol.Prostamol.domain.model.account.Account;

import java.util.UUID;

public interface GetAccountUseCase {
    Account execute(UUID accountId);
}
