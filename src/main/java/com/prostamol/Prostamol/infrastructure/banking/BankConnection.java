package com.prostamol.Prostamol.infrastructure.banking;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "bank_connections")
public class BankConnection {
    @Id UUID id;
    @Column(nullable = false) UUID userId;
    @Column(nullable = false) String bankName;
    @Column(nullable = false, length = 2) String country;
    @Column(unique = true) String stateHash;
    Instant stateExpiresAt;
    String sessionId;
    Instant validUntil;
    @Column(nullable = false) String status;
    @Column(nullable = false) LocalDate importFrom;
    Instant lastSyncedAt;
    String lastError;
    int skippedTransactions;
    protected BankConnection() {}
}
