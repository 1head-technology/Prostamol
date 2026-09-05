package com.prostamol.Prostamol.infrastructure.web.controller;

import com.prostamol.Prostamol.domain.model.budget.Budget;
import com.prostamol.Prostamol.domain.model.shared.DateRange;
import com.prostamol.Prostamol.domain.model.shared.Money;
import com.prostamol.Prostamol.domain.port.in.budget.AddBudgetLineUseCase;
import com.prostamol.Prostamol.domain.port.in.budget.CreateBudgetUseCase;
import com.prostamol.Prostamol.domain.port.in.budget.GetBudgetSummaryUseCase;
import com.prostamol.Prostamol.domain.port.in.budget.GetBudgetUseCase;
import com.prostamol.Prostamol.domain.port.in.budget.GetBudgetsUseCase;
import com.prostamol.Prostamol.domain.port.in.budget.UpdateBudgetLineUseCase;
import com.prostamol.Prostamol.domain.port.in.budget.UpdateBudgetUseCase;
import com.prostamol.Prostamol.infrastructure.web.dto.request.AddBudgetLineRequest;
import com.prostamol.Prostamol.infrastructure.web.dto.request.CreateBudgetRequest;
import com.prostamol.Prostamol.infrastructure.web.dto.request.UpdateBudgetLineRequest;
import com.prostamol.Prostamol.infrastructure.web.dto.request.UpdateBudgetRequest;
import com.prostamol.Prostamol.infrastructure.web.dto.response.BudgetResponse;
import com.prostamol.Prostamol.infrastructure.web.dto.response.BudgetSummaryResponse;
import com.prostamol.Prostamol.infrastructure.web.mapper.BudgetWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final GetBudgetUseCase getBudget;
    private final GetBudgetSummaryUseCase getSummary;
    private final UpdateBudgetUseCase updateBudget;
    private final AddBudgetLineUseCase addBudgetLine;
    private final UpdateBudgetLineUseCase updateBudgetLine;
    private final BudgetWebMapper mapper;

    public BudgetController(
        CreateBudgetUseCase createBudget,
        GetBudgetsUseCase getBudgets,
        GetBudgetUseCase getBudget,
        GetBudgetSummaryUseCase getSummary,
        UpdateBudgetUseCase updateBudget,
        AddBudgetLineUseCase addBudgetLine,
        UpdateBudgetLineUseCase updateBudgetLine,
        BudgetWebMapper mapper
    ) {
        this.createBudget = createBudget;
        this.getBudgets = getBudgets;
        this.getBudget = getBudget;
        this.getSummary = getSummary;
        this.updateBudget = updateBudget;
        this.addBudgetLine = addBudgetLine;
        this.updateBudgetLine = updateBudgetLine;
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

    @PatchMapping("/budgets/{budgetId}")
    public BudgetResponse patch(
        @AuthenticationPrincipal UUID userId,
        @PathVariable UUID budgetId,
        @Valid @RequestBody UpdateBudgetRequest request
    ) {
        Budget budget = getBudget.execute(budgetId);
        if (!budget.getUserId().equals(userId)) {
            throw new AccessDeniedException("Budget does not belong to the authenticated user");
        }

        return mapper.toResponse(updateBudget.execute(toUpdateCommand(budgetId, request)));
    }

    @PostMapping("/budgets/{budgetId}/lines")
    @ResponseStatus(HttpStatus.CREATED)
    public BudgetResponse addLine(
        @AuthenticationPrincipal UUID userId,
        @PathVariable UUID budgetId,
        @Valid @RequestBody AddBudgetLineRequest request
    ) {
        Budget budget = getBudget.execute(budgetId);
        if (!budget.getUserId().equals(userId)) {
            throw new AccessDeniedException("Budget does not belong to the authenticated user");
        }

        return mapper.toResponse(addBudgetLine.execute(toAddLineCommand(budgetId, request)));
    }

    @PatchMapping("/budgets/{budgetId}/lines/{lineId}")
    public BudgetResponse patchLine(
        @AuthenticationPrincipal UUID userId,
        @PathVariable UUID budgetId,
        @PathVariable UUID lineId,
        @Valid @RequestBody UpdateBudgetLineRequest request
    ) {
        Budget budget = getBudget.execute(budgetId);
        if (!budget.getUserId().equals(userId)) {
            throw new AccessDeniedException("Budget does not belong to the authenticated user");
        }

        return mapper.toResponse(updateBudgetLine.execute(toUpdateLineCommand(budgetId, lineId, request)));
    }

    // ── Admin endpoints ──────────────────────────────────────────────────────

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/users/{userId}/budgets")
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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users/{userId}/budgets")
    public List<BudgetResponse> listByAdmin(@PathVariable UUID userId) {
        return getBudgets.execute(userId).stream()
            .map(mapper::toResponse)
            .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/users/{userId}/budgets/{budgetId}")
    public BudgetResponse patchByAdmin(
        @PathVariable UUID userId,
        @PathVariable UUID budgetId,
        @Valid @RequestBody UpdateBudgetRequest request
    ) {
        Budget budget = getBudget.execute(budgetId);
        if (!budget.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Budget " + budgetId + " does not belong to user " + userId);
        }

        return mapper.toResponse(updateBudget.execute(toUpdateCommand(budgetId, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/users/{userId}/budgets/{budgetId}/lines")
    @ResponseStatus(HttpStatus.CREATED)
    public BudgetResponse addLineByAdmin(
        @PathVariable UUID userId,
        @PathVariable UUID budgetId,
        @Valid @RequestBody AddBudgetLineRequest request
    ) {
        Budget budget = getBudget.execute(budgetId);
        if (!budget.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Budget " + budgetId + " does not belong to user " + userId);
        }

        return mapper.toResponse(addBudgetLine.execute(toAddLineCommand(budgetId, request)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/users/{userId}/budgets/{budgetId}/lines/{lineId}")
    public BudgetResponse patchLineByAdmin(
        @PathVariable UUID userId,
        @PathVariable UUID budgetId,
        @PathVariable UUID lineId,
        @Valid @RequestBody UpdateBudgetLineRequest request
    ) {
        Budget budget = getBudget.execute(budgetId);
        if (!budget.getUserId().equals(userId)) {
            throw new IllegalArgumentException("Budget " + budgetId + " does not belong to user " + userId);
        }

        return mapper.toResponse(updateBudgetLine.execute(toUpdateLineCommand(budgetId, lineId, request)));
    }

    private UpdateBudgetUseCase.Command toUpdateCommand(UUID budgetId, UpdateBudgetRequest request) {
        return new UpdateBudgetUseCase.Command(
            budgetId,
            request.name(),
            request.from(),
            request.to(),
            request.status()
        );
    }

    private AddBudgetLineUseCase.Command toAddLineCommand(UUID budgetId, AddBudgetLineRequest request) {
        return new AddBudgetLineUseCase.Command(
            budgetId,
            request.categoryId(),
            Money.of(request.plannedAmount(), request.currency())
        );
    }

    private UpdateBudgetLineUseCase.Command toUpdateLineCommand(
        UUID budgetId,
        UUID lineId,
        UpdateBudgetLineRequest request
    ) {
        return new UpdateBudgetLineUseCase.Command(
            budgetId,
            lineId,
            request.categoryId(),
            request.plannedAmount(),
            request.currency()
        );
    }
}
