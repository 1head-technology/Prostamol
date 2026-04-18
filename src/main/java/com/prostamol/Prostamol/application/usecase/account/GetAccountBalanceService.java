package com.prostamol.Prostamol.application.usecase.account;

import com.prostamol.Prostamol.domain.model.account.Account;
import com.prostamol.Prostamol.domain.model.shared.Money;
import com.prostamol.Prostamol.domain.model.transaction.Transaction;
import com.prostamol.Prostamol.domain.model.transaction.TransactionType;
import com.prostamol.Prostamol.domain.port.in.account.GetAccountBalanceUseCase;
import com.prostamol.Prostamol.domain.port.out.AccountRepositoryPort;
import com.prostamol.Prostamol.domain.port.out.TransactionRepositoryPort;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public class GetAccountBalanceService implements GetAccountBalanceUseCase {

    private final AccountRepositoryPort accountRepository;
    private final TransactionRepositoryPort transactionRepository;

    public GetAccountBalanceService(
        AccountRepositoryPort accountRepository,
        TransactionRepositoryPort transactionRepository
    ) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Money execute(UUID accountId) {
        Account account = accountRepository
            .findById(accountId)
            .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));

        List<Transaction> transactions = transactionRepository.findAllByAccountId(accountId);
        String currency = account.getInitialBalance().currency();
        BigDecimal balance = account.getInitialBalance().amount();

        for (Transaction t : transactions) {
            if (t.getType() == TransactionType.INCOME || t.getType() == TransactionType.TRANSFER_IN) {
                balance = balance.add(t.getAmount().amount());
            }
            else {
                balance = balance.subtract(t.getAmount().amount());
            }
        }
        return Money.of(balance, currency);
    }
}
