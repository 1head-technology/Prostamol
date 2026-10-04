package com.prostamol.Prostamol.infrastructure.banking;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;

public interface BankAccountLinkRepository extends JpaRepository<BankAccountLink, UUID> {
    List<BankAccountLink> findAllByConnectionIdOrderByAccountId(UUID connectionId);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select a from BankAccountLink a where a.connectionId = :id order by a.accountId")
    List<BankAccountLink> lockAllByConnectionId(UUID id);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select a from BankAccountLink a where a.accountId = :id")
    Optional<BankAccountLink> lockById(UUID id);
}
