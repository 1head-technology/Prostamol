package com.prostamol.Prostamol.infrastructure.web.dto.response;

import com.prostamol.Prostamol.domain.model.user.User;

public record AuthResponse(String token, UserResponse user) {
    public AuthResponse(String token, User user) {
        this(token, new UserResponse(user.getId(), user.getEmail(), user.getName(),
            user.getDefaultCurrency(), user.getRole()));
    }
}
