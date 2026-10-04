package com.prostamol.Prostamol.infrastructure.banking;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bank_account_links", uniqueConstraints = @UniqueConstraint(columnNames = {"userId", "identityHash", "currency"}))
public class BankAccountLink {
    @Id UUID accountId;
    @Column(nullable = false) UUID userId;
    @Column(nullable = false, length = 64) String identityHash;
    @Column(nullable = false) UUID connectionId;
    @Column(nullable = false) String remoteUid;
    @Column(nullable = false, length = 3) String currency;
    @Column(precision = 19, scale = 4) BigDecimal bookedBalance;
    @Column(precision = 19, scale = 4) BigDecimal availableBalance;
    Instant balanceUpdatedAt;
    BigDecimal balance() { return bookedBalance != null ? bookedBalance : availableBalance; }
    protected BankAccountLink() {}
}
