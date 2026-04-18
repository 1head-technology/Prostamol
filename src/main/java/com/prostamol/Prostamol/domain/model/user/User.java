package com.prostamol.Prostamol.domain.model.user;

import java.util.UUID;

public class User {

    private final UUID id;
    private final String email;
    private final String name;
    private final String defaultCurrency;

    public User(
        UUID id,
        String email,
        String name,
        String defaultCurrency
    ) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.defaultCurrency = defaultCurrency;
    }

    public UUID getId() {
        return id;
    }
    public String getEmail() {
        return email;
    }
    public String getName() {
        return name;
    }
    public String getDefaultCurrency() {
        return defaultCurrency;
    }
}
