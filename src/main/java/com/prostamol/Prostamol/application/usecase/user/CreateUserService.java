package com.prostamol.Prostamol.application.usecase.user;

import com.prostamol.Prostamol.domain.model.user.User;
import com.prostamol.Prostamol.domain.port.in.user.CreateUserUseCase;
import com.prostamol.Prostamol.domain.port.out.UserRepositoryPort;

import java.util.UUID;

public class CreateUserService implements CreateUserUseCase {

    private final UserRepositoryPort userRepository;

    public CreateUserService(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User execute(Command command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new IllegalArgumentException("A user with email " + command.email() + " already exists");
        }

        return userRepository.save(new User(
            UUID.randomUUID(),
            command.email(),
            command.name(),
            command.defaultCurrency())
        );
    }
}
