package com.prostamol.Prostamol.application.usecase.auth;

import com.prostamol.Prostamol.application.usecase.user.CreateUserService;
import com.prostamol.Prostamol.domain.model.user.User;
import com.prostamol.Prostamol.domain.port.in.auth.SignupUseCase;
import com.prostamol.Prostamol.domain.port.in.user.CreateUserUseCase;
import com.prostamol.Prostamol.domain.port.out.PasswordEncoderPort;
import com.prostamol.Prostamol.domain.port.out.UserRepositoryPort;

public class SignupService implements SignupUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoderPort passwordEncoder;

    public SignupService(UserRepositoryPort userRepository, PasswordEncoderPort passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public User execute(Command command) {
        CreateUserService createUserService = new CreateUserService(this.userRepository, this.passwordEncoder);
        CreateUserUseCase.Command createUserCommand = new CreateUserUseCase.Command(
            command.email(),
            command.password(),
            command.name(),
            command.defaultCurrency()
        );

        return createUserService.execute(createUserCommand);
    }
}
