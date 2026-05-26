package com.prostamol.Prostamol.domain.port.in.account;

import java.util.UUID;

public interface DeleteAccountUseCase {
    void execute(Command command);

    record Command(
        UUID userId,
        UUID accountId
    ) {}
}
