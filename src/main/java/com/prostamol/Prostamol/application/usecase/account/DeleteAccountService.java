package com.prostamol.Prostamol.application.usecase.account;

import com.prostamol.Prostamol.domain.model.account.Account;
import com.prostamol.Prostamol.domain.model.transaction.Transaction;
import com.prostamol.Prostamol.domain.port.in.account.DeleteAccountUseCase;
import com.prostamol.Prostamol.domain.port.in.transaction.DeleteTransactionUseCase;
import com.prostamol.Prostamol.domain.port.out.AccountRepositoryPort;
import com.prostamol.Prostamol.domain.port.out.TransactionRepositoryPort;

import java.util.List;
import java.util.UUID;

public class DeleteAccountService implements DeleteAccountUseCase {

    private final AccountRepositoryPort accountRepository;
    private final TransactionRepositoryPort transactionRepository;

    public DeleteAccountService(AccountRepositoryPort accountRepository, TransactionRepositoryPort transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public void execute(Command command) {
        Account account = accountRepository
            .findById(command.accountId())
            .orElseThrow(() -> new IllegalArgumentException("Account not found: " + command.accountId()));

        if (!account.getUserId().equals(command.userId())) {
            throw new IllegalArgumentException("Transaction does not belong to user: " + command.userId());
        }

        List<Transaction> transactions = transactionRepository.findAllByAccountId(command.accountId());

        for (Transaction transaction : transactions) {
            transactionRepository.deleteById(transaction.getId());
        }

        accountRepository.deleteById(command.accountId());
    }
}
