package com.prostamol.Prostamol.application.usecase.auth;

import com.prostamol.Prostamol.domain.model.user.User;
import com.prostamol.Prostamol.domain.port.in.auth.SignupUseCase;
import com.prostamol.Prostamol.domain.port.in.user.CreateUserUseCase;

public class SignupService implements SignupUseCase {

    private final CreateUserUseCase createUser;

    public SignupService(CreateUserUseCase createUser) {
        this.createUser = createUser;
    }

    @Override
    public User execute(Command command) {
        return createUser.execute(new CreateUserUseCase.Command(
            command.email(),
            command.password(),
            command.name(),
            command.defaultCurrency()
        ));
    }
}
