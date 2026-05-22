package com.prostamol.Prostamol.domain.model.user;

import java.util.UUID;

public class User {

    private final UUID id;
    private final String email;
    private final String passwordHash;
    private final String name;
    private final String defaultCurrency;
    private final Role role;

    public User(
        UUID id,
        String email,
        String passwordHash,
        String name,
        String defaultCurrency,
        Role role
    ) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.name = name;
        this.defaultCurrency = defaultCurrency;
        this.role = role;
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
    public Role getRole() {
        return role;
    }
}
