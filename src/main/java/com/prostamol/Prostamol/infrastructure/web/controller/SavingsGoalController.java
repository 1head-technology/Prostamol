package com.prostamol.Prostamol.infrastructure.web.controller;

import com.prostamol.Prostamol.domain.model.shared.Money;
import com.prostamol.Prostamol.domain.port.in.savings.AddSavingsContributionUseCase;
import com.prostamol.Prostamol.domain.port.in.savings.CreateSavingsGoalUseCase;
import com.prostamol.Prostamol.domain.port.in.savings.GetSavingsGoalsUseCase;
import com.prostamol.Prostamol.infrastructure.web.dto.request.AddContributionRequest;
import com.prostamol.Prostamol.infrastructure.web.dto.request.CreateSavingsGoalRequest;
import com.prostamol.Prostamol.infrastructure.web.dto.response.SavingsGoalResponse;
import com.prostamol.Prostamol.infrastructure.web.mapper.SavingsGoalWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class SavingsGoalController {

    private final CreateSavingsGoalUseCase createGoal;
    private final GetSavingsGoalsUseCase getGoals;
    private final AddSavingsContributionUseCase addContribution;
    private final SavingsGoalWebMapper mapper;

    public SavingsGoalController(
        CreateSavingsGoalUseCase createGoal,
        GetSavingsGoalsUseCase getGoals,
        AddSavingsContributionUseCase addContribution,
        SavingsGoalWebMapper mapper
    ) {
        this.createGoal = createGoal;
        this.getGoals = getGoals;
        this.addContribution = addContribution;
        this.mapper = mapper;
    }

    // ── Authenticated user endpoints ─────────────────────────────────────────

    @PostMapping("/savings-goals")
    @ResponseStatus(HttpStatus.CREATED)
    public SavingsGoalResponse create(
        @AuthenticationPrincipal UUID userId,
        @Valid @RequestBody CreateSavingsGoalRequest request
    ) {
        return mapper.toResponse(createGoal.execute(new CreateSavingsGoalUseCase.Command(
            userId, request.name(),
            Money.of(request.targetAmount(), request.currency()),
            request.deadline()
        )));
    }

    @GetMapping("/savings-goals")
    public List<SavingsGoalResponse> list(@AuthenticationPrincipal UUID userId) {
        return getGoals.execute(userId).stream()
            .map(mapper::toResponse)
            .collect(Collectors.toList());
    }

    @PostMapping("/savings-goals/{goalId}/contributions")
    public SavingsGoalResponse contribute(
        @PathVariable UUID goalId,
        @Valid @RequestBody AddContributionRequest request
    ) {
        return mapper.toResponse(addContribution.execute(
            goalId, Money.of(request.amount(), request.currency())
        ));
    }

    // ── Admin endpoints ──────────────────────────────────────────────────────

    @PostMapping("/users/{userId}/savings-goals")
    @ResponseStatus(HttpStatus.CREATED)
    public SavingsGoalResponse createByAdmin(
        @PathVariable UUID userId,
        @Valid @RequestBody CreateSavingsGoalRequest request
    ) {
        return mapper.toResponse(createGoal.execute(new CreateSavingsGoalUseCase.Command(
            userId, request.name(),
            Money.of(request.targetAmount(), request.currency()),
            request.deadline()
        )));
    }

    @GetMapping("/users/{userId}/savings-goals")
    public List<SavingsGoalResponse> listByAdmin(@PathVariable UUID userId) {
        return getGoals.execute(userId).stream()
            .map(mapper::toResponse)
            .collect(Collectors.toList());
    }
}
