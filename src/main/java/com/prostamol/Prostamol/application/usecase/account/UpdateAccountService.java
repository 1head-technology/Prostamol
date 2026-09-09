package com.prostamol.Prostamol.application.usecase.account;

import com.prostamol.Prostamol.domain.model.account.Account;
import com.prostamol.Prostamol.domain.model.shared.Money;
import com.prostamol.Prostamol.domain.port.in.account.UpdateAccountUseCase;
import com.prostamol.Prostamol.domain.port.out.AccountRepositoryPort;
import com.prostamol.Prostamol.domain.port.out.TransactionRepositoryPort;
import org.springframework.security.access.AccessDeniedException;

public class UpdateAccountService implements UpdateAccountUseCase {

    private final AccountRepositoryPort accountRepository;
    private final TransactionRepositoryPort transactionRepository;

    public UpdateAccountService(
        AccountRepositoryPort accountRepository,
        TransactionRepositoryPort transactionRepository
    ) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Account execute(Command command) {
        Account account = accountRepository
            .findById(command.accountId())
            .orElseThrow(() -> new IllegalArgumentException("Account not found: " + command.accountId()));

        if (!account.getUserId().equals(command.userId())) {
            throw new AccessDeniedException("Account does not belong to the authenticated user");
        }

        Money currentBalance = account.getInitialBalance();
        Money updatedBalance = Money.of(
            command.initialBalance() != null ? command.initialBalance() : currentBalance.amount(),
            command.currency() != null ? command.currency() : currentBalance.currency()
        );

        if (!updatedBalance.currency().equals(currentBalance.currency())
            && !transactionRepository.findAllByAccountId(account.getId()).isEmpty()) {
            throw new IllegalArgumentException("Account currency cannot change while transactions exist");
        }

        return accountRepository.save(account.update(
            command.name() != null ? command.name() : account.getName(),
            command.type() != null ? command.type() : account.getType(),
            updatedBalance
        ));
    }
}
