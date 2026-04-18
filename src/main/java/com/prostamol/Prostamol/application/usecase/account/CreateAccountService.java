package com.prostamol.Prostamol.application.usecase.account;

import com.prostamol.Prostamol.domain.model.account.Account;
import com.prostamol.Prostamol.domain.port.in.account.CreateAccountUseCase;
import com.prostamol.Prostamol.domain.port.out.AccountRepositoryPort;
import com.prostamol.Prostamol.domain.port.out.UserRepositoryPort;

import java.util.UUID;

public class CreateAccountService implements CreateAccountUseCase {

    private final AccountRepositoryPort accountRepository;
    private final UserRepositoryPort userRepository;

    public CreateAccountService(
        AccountRepositoryPort accountRepository,
        UserRepositoryPort userRepository
    ) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Account execute(Command command) {
        userRepository
            .findById(command.userId())
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + command.userId()));

        return accountRepository.save(new Account(
            UUID.randomUUID(),
            command.userId(),
            command.name(),
            command.type(),
            command.initialBalance()
        ));
    }
}
