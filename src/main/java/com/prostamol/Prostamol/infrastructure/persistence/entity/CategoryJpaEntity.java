package com.prostamol.Prostamol.infrastructure.persistence.entity;

import com.prostamol.Prostamol.domain.model.category.CategoryType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "categories")
public class CategoryJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CategoryType type;

    @Column(name = "is_system", nullable = false)
    private boolean system;

    protected CategoryJpaEntity() {}

    public CategoryJpaEntity(
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
