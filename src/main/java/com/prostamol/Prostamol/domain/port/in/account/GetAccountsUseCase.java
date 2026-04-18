package com.prostamol.Prostamol.domain.port.in.account;

import com.prostamol.Prostamol.domain.model.account.Account;

import java.util.List;
import java.util.UUID;

public interface GetAccountsUseCase {
    List<Account> execute(UUID userId);
}
