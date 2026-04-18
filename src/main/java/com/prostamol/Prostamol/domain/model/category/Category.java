package com.prostamol.Prostamol.domain.model.category;

import java.util.UUID;

public class Category {

    private final UUID id;
    /** Null for system-level categories. */
    private final UUID userId;
    private final String name;
    private final CategoryType type;
    private final boolean system;

    public Category(
        UUID id,
        UUID userId,
        String name,
        CategoryType type,
        boolean system
    ) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.system = system;
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
    public CategoryType getType() {
        return type;
    }
    public boolean isSystem() {
        return system;
    }
}
