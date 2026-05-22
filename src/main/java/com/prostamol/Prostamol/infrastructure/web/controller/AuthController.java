package com.prostamol.Prostamol.infrastructure.web.controller;

import com.prostamol.Prostamol.domain.model.user.User;
import com.prostamol.Prostamol.domain.port.in.auth.AdminLoginUseCase;
import com.prostamol.Prostamol.domain.port.in.auth.LoginUseCase;
import com.prostamol.Prostamol.domain.port.in.auth.SignupUseCase;
import com.prostamol.Prostamol.infrastructure.security.JwtService;
import com.prostamol.Prostamol.infrastructure.web.dto.request.AdminLoginRequest;
import com.prostamol.Prostamol.infrastructure.web.dto.request.LoginRequest;
import com.prostamol.Prostamol.infrastructure.web.dto.request.SignupRequest;
import com.prostamol.Prostamol.infrastructure.web.dto.response.AuthResponse;
import jakarta.validation.Valid;
import org.jspecify.annotations.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final SignupUseCase signupUseCase;
    private final LoginUseCase loginUseCase;
    private final AdminLoginUseCase adminLoginUseCase;
    private final JwtService jwtService;

    public AuthController(
        SignupUseCase signupUseCase,
        LoginUseCase loginUseCase,
        AdminLoginUseCase adminLoginUseCase,
        JwtService jwtService
    ) {
        this.signupUseCase = signupUseCase;
        this.loginUseCase = loginUseCase;
        this.adminLoginUseCase = adminLoginUseCase;
        this.jwtService = jwtService;
    }

    @PostMapping("/auth/signup")
    public AuthResponse signup(@Valid @RequestBody @NonNull SignupRequest request) {
        SignupUseCase.Command signupCommand = new SignupUseCase.Command(
            request.email(),
            request.password(),
            request.name(),
            request.defaultCurrency()
        );

        User user = signupUseCase.execute(signupCommand);
        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());

        return new AuthResponse(token, user);
    }

    @PostMapping("/auth/login")
    public AuthResponse login(@Valid @RequestBody @NonNull LoginRequest request) {
        LoginUseCase.Command loginCommand = new LoginUseCase.Command(
            request.email(),
            request.password()
        );

        User user = loginUseCase.execute(loginCommand);
        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());

        return new AuthResponse(token, user);
    }

    // ── Admin endpoints ──────────────────────────────────────────────────────

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/auth/login/{userId}")
    public AuthResponse loginByAdmin(@PathVariable UUID userId) {
        AdminLoginUseCase.Command adminLoginCommand = new AdminLoginUseCase.Command(userId);

        User user = adminLoginUseCase.execute(adminLoginCommand);
        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());

        return new AuthResponse(token, user);
    }
}
