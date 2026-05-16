package com.prostamol.Prostamol.application.usecase.user;

import com.prostamol.Prostamol.domain.model.user.User;
import com.prostamol.Prostamol.domain.port.in.user.CreateUserUseCase;
import com.prostamol.Prostamol.domain.port.out.PasswordEncoderPort;
import com.prostamol.Prostamol.domain.port.out.UserRepositoryPort;

import java.util.UUID;

public class CreateUserService implements CreateUserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;

    public CreateUserService(UserRepositoryPort userRepository, PasswordEncoderPort passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User execute(Command command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new IllegalArgumentException("A user with email " + command.email() + " already exists");
        }

        String hashedPassword = passwordEncoder.encode(command.password());

        return userRepository.save(new User(
            UUID.randomUUID(),
            command.email(),
            hashedPassword,
            command.name(),
            command.defaultCurrency())
        );
    }
}
