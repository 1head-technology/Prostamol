package com.prostamol.Prostamol.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "budget_lines")
public class BudgetLineJpaEntity {

    @Id
    private UUID id;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "planned_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal plannedAmount;

    @Column(nullable = false, length = 3)
    private String currency;

    protected BudgetLineJpaEntity() {}

    public BudgetLineJpaEntity(UUID id, UUID categoryId, BigDecimal plannedAmount, String currency) {
        this.id = id;
        this.categoryId = categoryId;
        this.plannedAmount = plannedAmount;
        this.currency = currency;
    }

    public UUID getId() {
        return id;
    }
    public UUID getCategoryId() {
        return categoryId;
    }
    public BigDecimal getPlannedAmount() {
        return plannedAmount;
    }
    public String getCurrency() {
        return currency;
    }
}
