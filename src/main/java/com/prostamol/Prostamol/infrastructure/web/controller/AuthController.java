package com.prostamol.Prostamol.infrastructure.web.controller;

import com.prostamol.Prostamol.domain.model.user.User;
import com.prostamol.Prostamol.domain.port.in.auth.LoginUseCase;
import com.prostamol.Prostamol.domain.port.in.auth.SignupUseCase;
import com.prostamol.Prostamol.infrastructure.security.JwtService;
import com.prostamol.Prostamol.infrastructure.web.dto.request.LoginRequest;
import com.prostamol.Prostamol.infrastructure.web.dto.request.SignupRequest;
import com.prostamol.Prostamol.infrastructure.web.dto.response.AuthResponse;
import jakarta.validation.Valid;
import org.jspecify.annotations.NonNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final SignupUseCase signupUseCase;
    private final LoginUseCase loginUseCase;
    private final JwtService jwtService;

    public AuthController(
        SignupUseCase signupUseCase,
        LoginUseCase loginUseCase,
        JwtService jwtService
    ) {
        this.signupUseCase = signupUseCase;
        this.loginUseCase = loginUseCase;
        this.jwtService = jwtService;
    }

    @PostMapping("/signup")
    public AuthResponse login(@Valid @RequestBody @NonNull SignupRequest request) {
        SignupUseCase.Command signupCommand = new SignupUseCase.Command(
            request.email(),
            request.password(),
            request.name(),
            request.defaultCurrency()
        );

        User user = signupUseCase.execute(signupCommand);
        String token = jwtService.generateToken(user.getId(), user.getEmail());

        return new AuthResponse(token, user);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody @NonNull LoginRequest request) {
        LoginUseCase.Command loginCommand = new LoginUseCase.Command(
            request.email(),
            request.password()
        );

        User user = loginUseCase.execute(loginCommand);
        String token = jwtService.generateToken(user.getId(), user.getEmail());

        return new AuthResponse(token, user);
    }
}
