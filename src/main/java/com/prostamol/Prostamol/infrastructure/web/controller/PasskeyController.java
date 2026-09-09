package com.prostamol.Prostamol.infrastructure.web.controller;

import com.prostamol.Prostamol.infrastructure.security.JwtService;
import com.prostamol.Prostamol.infrastructure.security.passkey.PasskeyService;
import com.prostamol.Prostamol.infrastructure.web.dto.response.AuthResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class PasskeyController {

    private final PasskeyService passkeys;
    private final JwtService jwt;

    public PasskeyController(PasskeyService passkeys, JwtService jwt) {
        this.passkeys = passkeys;
        this.jwt = jwt;
    }

    public record RegistrationRequest(
        @NotNull UUID requestId,
        @NotBlank @Size(max = 100) String name,
        @NotNull JsonNode credential
    ) {}
    public record LoginRequest(
        @NotNull UUID requestId,
        @NotNull @Valid JsonNode credential
    ) {}

    @PostMapping("/passkeys/register/options")
    public PasskeyService.Options registrationOptions(@AuthenticationPrincipal UUID userId) throws IOException {
        return passkeys.startRegistration(userId);
    }

    @PostMapping("/passkeys/register/verify")
    @ResponseStatus(HttpStatus.CREATED)
    public PasskeyService.Summary register(
        @AuthenticationPrincipal UUID userId,
        @Valid @RequestBody RegistrationRequest request
    ) {
        return passkeys.finishRegistration(userId, request.requestId(), request.name(), request.credential().toString());
    }

    @GetMapping("/passkeys")
    public List<PasskeyService.Summary> list(@AuthenticationPrincipal UUID userId) {
        return passkeys.list(userId);
    }

    @DeleteMapping("/passkeys/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal UUID userId, @PathVariable UUID id) {
        passkeys.delete(userId, id);
    }

    @PostMapping("/auth/passkeys/options")
    public PasskeyService.Options loginOptions() throws IOException {
        return passkeys.startLogin();
    }

    @PostMapping("/auth/passkeys/verify")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        var user = passkeys.finishLogin(request.requestId(), request.credential().toString());
        return new AuthResponse(jwt.generateToken(user.getId(), user.getEmail(), user.getRole()), user);
    }
}
