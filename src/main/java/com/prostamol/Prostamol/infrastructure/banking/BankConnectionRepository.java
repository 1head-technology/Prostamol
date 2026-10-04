package com.prostamol.Prostamol.infrastructure.banking;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface BankConnectionRepository extends JpaRepository<BankConnection, UUID> {
    List<BankConnection> findAllByUserId(UUID userId);
    List<BankConnection> findAllByStatus(String status);
    Optional<BankConnection> findByStateHash(String stateHash);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from BankConnection c where c.id = :id")
    Optional<BankConnection> lockById(UUID id);
}
