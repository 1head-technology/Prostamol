package com.prostamol.Prostamol.domain.port.in.auth;

import com.prostamol.Prostamol.domain.model.user.User;

public interface LoginUseCase {
    User execute(Command command);

    record Command(
        String email,
        String password
    ) {}
}
