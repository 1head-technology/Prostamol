package com.prostamol.Prostamol.infrastructure.web.mapper;

import com.prostamol.Prostamol.domain.model.account.Account;
import com.prostamol.Prostamol.domain.model.shared.Money;
import com.prostamol.Prostamol.infrastructure.web.dto.response.AccountBalanceResponse;
import com.prostamol.Prostamol.infrastructure.web.dto.response.AccountResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AccountWebMapper {

    public AccountResponse toResponse(Account account) {
        return new AccountResponse(
            account.getId(),
            account.getUserId(),
            account.getName(),
            account.getType(),
            account.getInitialBalance().amount(),
            account.getInitialBalance().currency()
        );
    }

    public AccountBalanceResponse toBalanceResponse(UUID accountId, Money balance) {
        return new AccountBalanceResponse(
            accountId,
            balance.amount(),
            balance.currency()
        );
    }
}
