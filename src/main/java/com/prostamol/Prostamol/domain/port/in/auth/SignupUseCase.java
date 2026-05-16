package com.prostamol.Prostamol.domain.port.in.auth;

import com.prostamol.Prostamol.domain.model.user.User;

public interface SignupUseCase {
    User execute(Command command);

    record Command(
        String email,
        String password,
        String name,
        String defaultCurrency
    ) {}
}
