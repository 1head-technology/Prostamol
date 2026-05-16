package com.prostamol.Prostamol.infrastructure.web.dto.response;

import com.prostamol.Prostamol.domain.model.user.User;

public record AuthResponse(
    String token,
    User user
) {}
