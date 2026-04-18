package com.prostamol.Prostamol.application.usecase.account;

import com.prostamol.Prostamol.domain.model.account.Account;
import com.prostamol.Prostamol.domain.port.in.account.GetAccountsUseCase;
import com.prostamol.Prostamol.domain.port.out.AccountRepositoryPort;

import java.util.List;
import java.util.UUID;

public class GetAccountsService implements GetAccountsUseCase {

    private final AccountRepositoryPort accountRepository;

    public GetAccountsService(AccountRepositoryPort accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public List<Account> execute(UUID userId) {
        return accountRepository.findAllByUserId(userId);
    }
}
