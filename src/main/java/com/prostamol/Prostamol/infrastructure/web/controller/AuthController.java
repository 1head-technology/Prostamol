package com.prostamol.Prostamol.infrastructure.web.controller;

import com.prostamol.Prostamol.domain.model.user.User;
import com.prostamol.Prostamol.domain.port.in.auth.LoginUseCase;
import com.prostamol.Prostamol.infrastructure.security.JwtService;
import com.prostamol.Prostamol.infrastructure.web.dto.request.LoginRequest;
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

    private final LoginUseCase loginUseCase;
    private final JwtService jwtService;

    public AuthController(LoginUseCase loginUseCase, JwtService jwtService) {
        this.loginUseCase = loginUseCase;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody @NonNull LoginRequest request) {
        User user = loginUseCase.execute(new LoginUseCase.Command(request.email(), request.password()));
        String token = jwtService.generateToken(user.getId(), user.getEmail());

        return new AuthResponse(token, user.getId(), user.getEmail());
    }
}
