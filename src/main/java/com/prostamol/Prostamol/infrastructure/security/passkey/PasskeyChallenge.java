package com.prostamol.Prostamol.infrastructure.security.passkey;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "passkey_challenges", indexes = @Index(name = "idx_passkey_expiry", columnList = "expiresAt"))
public class PasskeyChallenge {
    @Id
    private UUID id;
    @Column(nullable = false)
    private String ceremony;
    private UUID userId;
    @Column(nullable = false, columnDefinition = "text")
    private String optionsJson;
    @Column(nullable = false)
    private Instant expiresAt;

    protected PasskeyChallenge() {}
    public PasskeyChallenge(String ceremony, UUID userId, String optionsJson, Instant expiresAt) {
        this.id = UUID.randomUUID();
        this.ceremony = ceremony;
        this.userId = userId;
        this.optionsJson = optionsJson;
        this.expiresAt = expiresAt;
    }
    public UUID getId() { return id; }
    public String getCeremony() { return ceremony; }
    public UUID getUserId() { return userId; }
    public String getOptionsJson() { return optionsJson; }
    public Instant getExpiresAt() { return expiresAt; }
}
