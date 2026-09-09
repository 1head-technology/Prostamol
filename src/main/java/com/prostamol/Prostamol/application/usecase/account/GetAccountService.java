package com.prostamol.Prostamol.application.usecase.account;

import com.prostamol.Prostamol.domain.model.account.Account;
import com.prostamol.Prostamol.domain.port.in.account.GetAccountUseCase;
import com.prostamol.Prostamol.domain.port.out.AccountRepositoryPort;
import org.springframework.security.access.AccessDeniedException;

import java.util.UUID;

public class GetAccountService implements GetAccountUseCase {

    private final AccountRepositoryPort accountRepository;

    public GetAccountService(AccountRepositoryPort accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Account execute(UUID userId, UUID accountId) {
        Account account = accountRepository
            .findById(accountId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        if (!account.getUserId().equals(userId)) {
            throw new AccessDeniedException("Account does not belong to the authenticated user");
        }

        return account;
    }
}
