package com.prostamol.Prostamol.application.usecase.account;

import com.prostamol.Prostamol.domain.model.account.Account;
import com.prostamol.Prostamol.domain.model.shared.Money;
import com.prostamol.Prostamol.domain.port.in.account.UpdateAccountUseCase;
import com.prostamol.Prostamol.domain.port.out.AccountRepositoryPort;

public class UpdateAccountService implements UpdateAccountUseCase {

    private final AccountRepositoryPort accountRepository;

    public UpdateAccountService(AccountRepositoryPort accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Account execute(Command command) {
        Account account = accountRepository
            .findById(command.accountId())
            .orElseThrow(() -> new IllegalArgumentException("Account not found: " + command.accountId()));

        Money currentBalance = account.getInitialBalance();
        Money updatedBalance = Money.of(
            command.initialBalance() != null ? command.initialBalance() : currentBalance.amount(),
            command.currency() != null ? command.currency() : currentBalance.currency()
        );

        return accountRepository.save(account.update(
            command.name() != null ? command.name() : account.getName(),
            command.type() != null ? command.type() : account.getType(),
            updatedBalance
        ));
    }
}
