package com.prostamol.Prostamol.domain.port.in.auth;

import com.prostamol.Prostamol.domain.model.user.User;

import java.util.UUID;

public interface AdminLoginUseCase {
    User execute(Command command);

    record Command(
        UUID userId
    ) {}
}
