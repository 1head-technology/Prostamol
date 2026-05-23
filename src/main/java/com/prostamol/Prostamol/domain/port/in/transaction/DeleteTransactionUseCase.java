package com.prostamol.Prostamol.domain.port.in.transaction;

import java.util.UUID;

public interface DeleteTransactionUseCase {
    void execute(Command command);

    record Command(
        UUID userId,
        UUID transactionId
    ) {}
}
