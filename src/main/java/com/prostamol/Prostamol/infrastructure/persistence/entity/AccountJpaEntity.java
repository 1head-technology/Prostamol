package com.prostamol.Prostamol.infrastructure.persistence.entity;

import com.prostamol.Prostamol.domain.model.account.AccountType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "accounts")
public class AccountJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type;

    @Column(name = "initial_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal initialBalance;

    @Column(nullable = false, length = 3)
    private String currency;

    protected AccountJpaEntity() {}

    public AccountJpaEntity(
        UUID id,
        UUID userId,
        String name,
        AccountType type,
        BigDecimal initialBalance,
        String currency
    ) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.initialBalance = initialBalance;
        this.currency = currency;
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
    public BigDecimal getInitialBalance() {
        return initialBalance;
    }
    public String getCurrency() {
        return currency;
    }
}
