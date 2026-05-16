package com.prostamol.Prostamol.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "users")
public class UserJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String name;

    @Column(name = "default_currency", nullable = false, length = 3)
    private String defaultCurrency;

    protected UserJpaEntity() {}

    public UserJpaEntity(UUID id, String email, String passwordHash, String name, String defaultCurrency) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.defaultCurrency = defaultCurrency;
    }

    public UUID getId() {
        return id;
    }
    public String getEmail() {
        return email;
    }
    public String getPasswordHash() {
        return passwordHash;
    }
    public String getName() {
        return name;
    }
    public String getDefaultCurrency() {
        return defaultCurrency;
    }
}
