package com.prostamol.Prostamol.domain.port.in.transaction;

import com.prostamol.Prostamol.domain.model.shared.Money;
import com.prostamol.Prostamol.domain.model.transaction.Transaction;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface RecordTransferUseCase {
    List<Transaction> execute(Command command);

    record Command(
        UUID userId,
        UUID sourceAccountId,
        UUID destinationAccountId,
        Money amount,
        LocalDate date,
        String description
    ) {}
}
