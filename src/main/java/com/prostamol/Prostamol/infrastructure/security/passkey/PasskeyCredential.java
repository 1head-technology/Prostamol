package com.prostamol.Prostamol.infrastructure.security.passkey;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "passkey_credentials", indexes = @Index(name = "idx_passkey_user", columnList = "userId"))
public class PasskeyCredential {
    @Id
    private UUID id;
    @Version
    private Long version;
    @Column(nullable = false, unique = true, length = 1400)
    private String credentialId;
    @Column(nullable = false)
    private UUID userId;
    @Column(nullable = false, columnDefinition = "text")
    private String publicKeyCose;
    @Column(nullable = false)
    private long signatureCount;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false)
    private Instant createdAt;
    private Instant lastUsedAt;

    protected PasskeyCredential() {}

    public PasskeyCredential(String credentialId, UUID userId, String publicKeyCose,
                             long signatureCount, String name) {
        this.id = UUID.randomUUID();
        this.credentialId = credentialId;
        this.userId = userId;
        this.publicKeyCose = publicKeyCose;
        this.signatureCount = signatureCount;
        this.name = name;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getCredentialId() { return credentialId; }
    public UUID getUserId() { return userId; }
    public String getPublicKeyCose() { return publicKeyCose; }
    public long getSignatureCount() { return signatureCount; }
    public String getName() { return name; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getLastUsedAt() { return lastUsedAt; }
    public void recordUse(long count) {
        signatureCount = count;
        lastUsedAt = Instant.now();
    }
}
