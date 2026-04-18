package com.prostamol.Prostamol.infrastructure.web.controller;

import com.prostamol.Prostamol.domain.model.shared.Money;
import com.prostamol.Prostamol.domain.port.in.account.CreateAccountUseCase;
import com.prostamol.Prostamol.domain.port.in.account.GetAccountBalanceUseCase;
import com.prostamol.Prostamol.domain.port.in.account.GetAccountsUseCase;
import com.prostamol.Prostamol.infrastructure.web.dto.request.CreateAccountRequest;
import com.prostamol.Prostamol.infrastructure.web.dto.response.AccountBalanceResponse;
import com.prostamol.Prostamol.infrastructure.web.dto.response.AccountResponse;
import com.prostamol.Prostamol.infrastructure.web.mapper.AccountWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
    private final AccountWebMapper mapper;

    public AccountController(
        CreateAccountUseCase createAccount,
        GetAccountsUseCase getAccounts,
        GetAccountBalanceUseCase getBalance,
        AccountWebMapper mapper
    ) {
        this.createAccount = createAccount;
        this.getAccounts = getAccounts;
        this.getBalance = getBalance;
        this.mapper = mapper;
    }

    @PostMapping("/users/{userId}/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse create(@PathVariable UUID userId, @Valid @RequestBody CreateAccountRequest request) {
        return mapper.toResponse(createAccount.execute(new CreateAccountUseCase.Command(
            userId,
            request.name(),
            request.type(),
            Money.of(request.initialBalance(), request.currency())
        )));
    }

    @GetMapping("/users/{userId}/accounts")
    public List<AccountResponse> list(@PathVariable UUID userId) {
        return getAccounts.execute(userId).stream().map(mapper::toResponse).collect(Collectors.toList());
    }

    @GetMapping("/accounts/{accountId}/balance")
    public AccountBalanceResponse balance(@PathVariable UUID accountId) {
        Money balance = getBalance.execute(accountId);
        return mapper.toBalanceResponse(accountId, balance);
    }
}
