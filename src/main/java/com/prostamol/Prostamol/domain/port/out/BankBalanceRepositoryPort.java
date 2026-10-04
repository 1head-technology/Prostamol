package com.prostamol.Prostamol.domain.port.out;

import com.prostamol.Prostamol.domain.model.shared.Money;
import java.util.Optional;
import java.util.UUID;

public interface BankBalanceRepositoryPort {
    /** Empty for manual accounts; linked accounts without a bank balance must report unavailable. */
    Optional<Money> findBankBalance(UUID accountId);
}
