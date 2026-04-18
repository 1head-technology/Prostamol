package com.prostamol.Prostamol.domain.port.in.user;

import com.prostamol.Prostamol.domain.model.user.User;

import java.util.UUID;

public interface GetUserUseCase {
    User execute(UUID userId);
}
