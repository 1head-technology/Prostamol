package com.prostamol.Prostamol.infrastructure.banking;

import com.prostamol.Prostamol.domain.model.shared.Money;
import com.prostamol.Prostamol.domain.port.out.BankBalanceRepositoryPort;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class BankBalanceAdapter implements BankBalanceRepositoryPort {
    private final BankAccountLinkRepository links;
    public BankBalanceAdapter(BankAccountLinkRepository links) { this.links = links; }
    @Override public Optional<Money> findBankBalance(UUID accountId) {
        return links.findById(accountId).map(link -> {
            if (link.bookedBalance == null) throw new BankingException(409, "Bank booked balance is unavailable; check connection sync status");
            return Money.of(link.bookedBalance, link.currency);
        });
    }
}
