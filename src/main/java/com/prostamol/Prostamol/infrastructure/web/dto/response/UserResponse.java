package com.prostamol.Prostamol.infrastructure.web.dto.response;

import com.prostamol.Prostamol.domain.model.user.Role;

import java.util.UUID;

public record UserResponse(UUID id, String email, String name, String defaultCurrency, Role role) {}
