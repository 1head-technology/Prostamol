package com.prostamol.Prostamol.infrastructure.persistence.entity;

import com.prostamol.Prostamol.domain.model.budget.BudgetStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "budgets")
public class BudgetJpaEntity {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String name;

    @Column(name = "period_from", nullable = false)
    private LocalDate periodFrom;

    @Column(name = "period_to", nullable = false)
    private LocalDate periodTo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BudgetStatus status;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "budget_id")
    private List<BudgetLineJpaEntity> lines = new ArrayList<>();

    protected BudgetJpaEntity() {}

    public BudgetJpaEntity(
        UUID id,
        UUID userId,
        String name,
        LocalDate periodFrom,
        LocalDate periodTo,
        BudgetStatus status,
        List<BudgetLineJpaEntity> lines
    ) {
        this.id = id;
        this.userId = userId;
        this.name = name;
        this.periodFrom = periodFrom;
        this.periodTo = periodTo;
        this.status = status;
        this.lines = new ArrayList<>(lines);
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
    public LocalDate getPeriodFrom() {
        return periodFrom;
    }
    public LocalDate getPeriodTo() {
        return periodTo;
    }
    public BudgetStatus getStatus() {
        return status;
    }
    public List<BudgetLineJpaEntity> getLines() {
        return lines;
    }
}
