package com.prostamol.Prostamol.infrastructure.web.controller;

import com.prostamol.Prostamol.domain.model.shared.DateRange;
import com.prostamol.Prostamol.domain.model.shared.Money;
import com.prostamol.Prostamol.domain.port.in.budget.CreateBudgetUseCase;
import com.prostamol.Prostamol.domain.port.in.budget.GetBudgetSummaryUseCase;
import com.prostamol.Prostamol.domain.port.in.budget.GetBudgetsUseCase;
import com.prostamol.Prostamol.infrastructure.web.dto.request.CreateBudgetRequest;
import com.prostamol.Prostamol.infrastructure.web.dto.response.BudgetResponse;
import com.prostamol.Prostamol.infrastructure.web.dto.response.BudgetSummaryResponse;
import com.prostamol.Prostamol.infrastructure.web.mapper.BudgetWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class BudgetController {

    private final CreateBudgetUseCase createBudget;
    private final GetBudgetsUseCase getBudgets;
    private final GetBudgetSummaryUseCase getSummary;
    private final BudgetWebMapper mapper;

    public BudgetController(
        CreateBudgetUseCase createBudget,
        GetBudgetsUseCase getBudgets,
        GetBudgetSummaryUseCase getSummary,
        BudgetWebMapper mapper
    ) {
        this.createBudget = createBudget;
        this.getBudgets = getBudgets;
        this.getSummary = getSummary;
        this.mapper = mapper;
    }

    // ── Authenticated user endpoints ─────────────────────────────────────────

    @PostMapping("/budgets")
    @ResponseStatus(HttpStatus.CREATED)
    public BudgetResponse create(
        @AuthenticationPrincipal UUID userId,
        @Valid @RequestBody CreateBudgetRequest request
    ) {
        var lines = request.lines().stream()
            .map(l -> new CreateBudgetUseCase.LineCommand(l.categoryId(), Money.of(l.plannedAmount(), l.currency())))
            .collect(Collectors.toList());

        return mapper.toResponse(createBudget.execute(
            new CreateBudgetUseCase.Command(
                userId,
                request.name(),
                new DateRange(request.from(), request.to()),
                lines
            )
        ));
    }

    @GetMapping("/budgets")
    public List<BudgetResponse> list(@AuthenticationPrincipal UUID userId) {
        return getBudgets.execute(userId).stream()
            .map(mapper::toResponse)
            .collect(Collectors.toList());
    }

    @GetMapping("/budgets/{budgetId}/summary")
    public BudgetSummaryResponse summary(@PathVariable UUID budgetId) {
        return mapper.toSummaryResponse(getSummary.execute(budgetId));
    }

    // ── Admin endpoints ──────────────────────────────────────────────────────

    @PostMapping("/users/{userId}/budgets")
    @ResponseStatus(HttpStatus.CREATED)
    public BudgetResponse createByAdmin(
        @PathVariable UUID userId,
        @Valid @RequestBody CreateBudgetRequest request
    ) {
        var lines = request.lines().stream()
            .map(l -> new CreateBudgetUseCase.LineCommand(l.categoryId(), Money.of(l.plannedAmount(), l.currency())))
            .collect(Collectors.toList());

        return mapper.toResponse(createBudget.execute(
            new CreateBudgetUseCase.Command(
                userId,
                request.name(),
                new DateRange(request.from(), request.to()),
                lines
            )
        ));
    }

    @GetMapping("/users/{userId}/budgets")
    public List<BudgetResponse> listByAdmin(@PathVariable UUID userId) {
        return getBudgets.execute(userId).stream()
            .map(mapper::toResponse)
            .collect(Collectors.toList());
    }
}
