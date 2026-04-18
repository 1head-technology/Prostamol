package com.prostamol.Prostamol.infrastructure.persistence.mapper;

import com.prostamol.Prostamol.domain.model.account.Account;
import com.prostamol.Prostamol.domain.model.shared.Money;
import com.prostamol.Prostamol.infrastructure.persistence.entity.AccountJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class AccountPersistenceMapper {

    public Account toDomain(AccountJpaEntity entity) {
        return new Account(
            entity.getId(),
            entity.getUserId(),
            entity.getName(),
            entity.getType(),
            Money.of(entity.getInitialBalance(),
            entity.getCurrency())
        );
    }

    public AccountJpaEntity toJpaEntity(Account account) {
        return new AccountJpaEntity(
            account.getId(),
            account.getUserId(),
            account.getName(),
            account.getType(),
            account.getInitialBalance().amount(),
            account.getInitialBalance().currency()
        );
    }
}
