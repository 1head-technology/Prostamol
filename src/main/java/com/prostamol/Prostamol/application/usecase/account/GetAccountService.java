package com.prostamol.Prostamol.application.usecase.account;

import com.prostamol.Prostamol.domain.model.account.Account;
import com.prostamol.Prostamol.domain.port.in.account.GetAccountUseCase;
import com.prostamol.Prostamol.domain.port.out.AccountRepositoryPort;

import java.util.UUID;

public class GetAccountService implements GetAccountUseCase {

    private final AccountRepositoryPort accountRepository;

    public GetAccountService(AccountRepositoryPort accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Account execute(UUID accountId) {
        return accountRepository
            .findById(accountId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));
    }
}
