package com.prostamol.Prostamol.application.usecase.user;

import com.prostamol.Prostamol.domain.model.user.User;
import com.prostamol.Prostamol.domain.port.in.user.GetUserUseCase;
import com.prostamol.Prostamol.domain.port.out.UserRepositoryPort;

import java.util.UUID;

public class GetUserService implements GetUserUseCase {

    private final UserRepositoryPort userRepository;

    public GetUserService(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User execute(UUID userId) {
        return userRepository
            .findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
    }
}
