package com.prostamol.Prostamol.domain.model.account;

import com.prostamol.Prostamol.domain.model.shared.Money;

import java.util.UUID;

public class Account {

    private final UUID id;
    private final UUID userId;
    private final String name;
    private final AccountType type;
    private final Money initialBalance;

    public Account(
        UUID id,
        UUID userId,
        String name,
        AccountType type,
        Money initialBalance
    ) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.initialBalance = initialBalance;
    }

    public Account update(String name, AccountType type, Money initialBalance) {
        return new Account(id, userId, name, type, initialBalance);
    }

    public UUID getId() {
        return id;
    }
    public UUID getUserId() {
        return userId;
    }
    public String getName() {
        return name;
    }
    public AccountType getType() {
        return type;
    }
    public Money getInitialBalance() {
        return initialBalance;
    }
}
