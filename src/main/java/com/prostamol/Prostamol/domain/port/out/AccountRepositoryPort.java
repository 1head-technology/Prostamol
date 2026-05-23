package com.prostamol.Prostamol.domain.port.out;

import com.prostamol.Prostamol.domain.model.account.Account;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepositoryPort {
    Account save(Account account);
    Optional<Account> findById(UUID id);
    List<Account> findAllByUserId(UUID userId);
    void delete(UUID accountId);
}
