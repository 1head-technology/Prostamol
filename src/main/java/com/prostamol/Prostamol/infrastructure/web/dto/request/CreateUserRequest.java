package com.prostamol.Prostamol.infrastructure.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
    @NotBlank @Email String email,
    @NotBlank String name,
    @NotBlank @Size(min = 3, max = 3) String defaultCurrency
) {}
