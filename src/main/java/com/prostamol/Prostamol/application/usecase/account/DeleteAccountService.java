package com.prostamol.Prostamol.application.usecase.account;

import com.prostamol.Prostamol.domain.port.in.account.DeleteAccountUseCase;
import com.prostamol.Prostamol.domain.port.out.AccountRepositoryPort;

import java.util.UUID;

public class DeleteAccountService implements DeleteAccountUseCase {

    private final AccountRepositoryPort accountRepository;

    public DeleteAccountService(AccountRepositoryPort accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public void execute(UUID accountId) {
        accountRepository
            .findById(accountId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        accountRepository.delete(accountId);
    }
}
