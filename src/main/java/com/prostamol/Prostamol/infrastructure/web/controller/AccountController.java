package com.prostamol.Prostamol.infrastructure.web.controller;

import com.prostamol.Prostamol.domain.model.shared.Money;
import com.prostamol.Prostamol.domain.port.in.account.*;
import com.prostamol.Prostamol.infrastructure.web.dto.request.CreateAccountRequest;
import com.prostamol.Prostamol.infrastructure.web.dto.request.UpdateAccountRequest;
import com.prostamol.Prostamol.infrastructure.web.dto.response.AccountBalanceResponse;
import com.prostamol.Prostamol.infrastructure.web.dto.response.AccountResponse;
import com.prostamol.Prostamol.infrastructure.web.mapper.AccountWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class AccountController {

    private final CreateAccountUseCase createAccount;
    private final GetAccountsUseCase getAccounts;
    private final GetAccountBalanceUseCase getBalance;
    private final UpdateAccountUseCase updateAccount;
    private final DeleteAccountUseCase deleteAccount;
    private final AccountWebMapper mapper;

    public AccountController(
        CreateAccountUseCase createAccount,
        GetAccountsUseCase getAccounts,
        GetAccountBalanceUseCase getBalance,
        UpdateAccountUseCase updateAccount,
        DeleteAccountUseCase deleteAccount,
        AccountWebMapper mapper
    ) {
        this.createAccount = createAccount;
        this.getAccounts = getAccounts;
        this.getBalance = getBalance;
        this.updateAccount = updateAccount;
        this.deleteAccount = deleteAccount;
        this.mapper = mapper;
    }

    // ── Authenticated user endpoints ─────────────────────────────────────────

    @PostMapping("/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(@AuthenticationPrincipal UUID userId, @RequestBody @Valid CreateAccountRequest request) {
        return mapper.toResponse(createAccount.execute(new CreateAccountUseCase.Command(
            userId,
            request.name(),
            request.type(),
            Money.of(request.initialBalance(), request.currency())
        )));
    }

    @GetMapping("/accounts")
    public List<AccountResponse> list(@AuthenticationPrincipal UUID userId) {
        return getAccounts.execute(userId).stream().map(mapper::toResponse).collect(Collectors.toList());
    }

    @GetMapping("/accounts/{accountId}/balance")
    public AccountBalanceResponse balance(
        @AuthenticationPrincipal UUID userId,
        @PathVariable UUID accountId
    ) {
        Money balance = getBalance.execute(userId, accountId);
        return mapper.toBalanceResponse(accountId, balance);
    }

    @PatchMapping("/accounts/{accountId}")
    public AccountResponse patchAccount(
        @AuthenticationPrincipal UUID userId,
        @PathVariable UUID accountId,
        @Valid @RequestBody UpdateAccountRequest request
    ) {
        return mapper.toResponse(updateAccount.execute(toUpdateCommand(userId, accountId, request)));
    }

    @DeleteMapping("/accounts/{accountId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(@AuthenticationPrincipal UUID userId, @PathVariable UUID accountId) {
        DeleteAccountUseCase.Command deleteAccountCommand = new DeleteAccountUseCase.Command(userId, accountId);
        deleteAccount.execute(deleteAccountCommand);
    }

    // ── Admin endpoints ──────────────────────────────────────────────────────

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/users/{userId}/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse createByAdmin(@PathVariable UUID userId, @Valid @RequestBody CreateAccountRequest request) {
        CreateAccountUseCase.Command command = new CreateAccountUseCase.Command(
            userId,
            request.name(),
            request.type(),
            Money.of(request.initialBalance(), request.currency())
        );

        return mapper.toResponse(createAccount.execute(command));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/users/{userId}/accounts")
    public List<AccountResponse> listByAdmin(@PathVariable UUID userId) {
        return getAccounts.execute(userId).stream().map(mapper::toResponse).collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/admin/users/{userId}/accounts/{accountId}")
    public AccountResponse patchAccountByAdmin(
        @PathVariable UUID userId,
        @PathVariable UUID accountId,
        @Valid @RequestBody UpdateAccountRequest request
    ) {
        return mapper.toResponse(updateAccount.execute(toUpdateCommand(userId, accountId, request)));
    }

    private UpdateAccountUseCase.Command toUpdateCommand(
        UUID userId,
        UUID accountId,
        UpdateAccountRequest request
    ) {
        return new UpdateAccountUseCase.Command(
            userId,
            accountId,
            request.name(),
            request.type(),
            request.initialBalance(),
            request.currency()
        );
    }
}
