package com.prostamol.Prostamol.infrastructure.web.controller;

import com.prostamol.Prostamol.domain.port.in.user.CreateUserUseCase;
import com.prostamol.Prostamol.domain.port.in.user.GetUserUseCase;
import com.prostamol.Prostamol.infrastructure.web.dto.request.CreateUserRequest;
import com.prostamol.Prostamol.infrastructure.web.dto.response.UserResponse;
import com.prostamol.Prostamol.infrastructure.web.mapper.UserWebMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final CreateUserUseCase createUserUseCase;
    private final GetUserUseCase getUserUseCase;
    private final UserWebMapper mapper;

    public UserController(
        CreateUserUseCase createUserUseCase,
        GetUserUseCase getUserUseCase,
        UserWebMapper mapper
    ) {
        this.createUserUseCase = createUserUseCase;
        this.getUserUseCase = getUserUseCase;
        this.mapper = mapper;
    }

    // ── Authenticated user endpoints ─────────────────────────────────────────

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal UUID userId) {
        return mapper.toResponse(getUserUseCase.execute(userId));
    }

    // ── Admin endpoints ──────────────────────────────────────────────────────

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createByAdmin(@Valid @RequestBody CreateUserRequest request) {
        return mapper.toResponse(createUserUseCase.execute(
            new CreateUserUseCase.Command(
                request.email(),
                request.password(),
                request.name(),
                request.defaultCurrency()
            )
        ));
    }

    @GetMapping("/{id}")
    public UserResponse getByAdmin(@PathVariable UUID id) {
        return mapper.toResponse(getUserUseCase.execute(id));
    }
}
