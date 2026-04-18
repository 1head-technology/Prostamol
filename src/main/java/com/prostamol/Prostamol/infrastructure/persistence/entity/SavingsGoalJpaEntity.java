package com.prostamol.Prostamol.infrastructure.persistence.entity;

import com.prostamol.Prostamol.domain.model.savings.SavingsGoalStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "savings_goals")
public class SavingsGoalJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String name;

    @Column(name = "target_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal targetAmount;

    @Column(name = "current_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal currentAmount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column
    private LocalDate deadline;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SavingsGoalStatus status;

    protected SavingsGoalJpaEntity() {}

    public SavingsGoalJpaEntity(
        UUID id,
        UUID userId,
        String name,
        BigDecimal targetAmount,
        BigDecimal currentAmount,
        String currency,
        LocalDate deadline,
        SavingsGoalStatus status
    ) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.targetAmount = targetAmount;
        this.currentAmount = currentAmount;
        this.currency = currency;
        this.deadline = deadline;
        this.status = status;
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
    public BigDecimal getTargetAmount() {
        return targetAmount;
    }
    public BigDecimal getCurrentAmount() {
        return currentAmount;
    }
    public String getCurrency() {
        return currency;
    }
    public LocalDate getDeadline() {
        return deadline;
    }
    public SavingsGoalStatus getStatus() {
        return status;
    }
}
