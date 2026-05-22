package com.prostamol.Prostamol.application.usecase.auth;

import com.prostamol.Prostamol.domain.model.user.User;
import com.prostamol.Prostamol.domain.port.in.auth.AdminLoginUseCase;
import com.prostamol.Prostamol.domain.port.out.UserRepositoryPort;

public class AdminLoginService implements AdminLoginUseCase {

    private final UserRepositoryPort userRepository;

    public AdminLoginService(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User execute(Command command) {
        return userRepository
            .findById(command.userId())
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + command.userId()));
    }
}
