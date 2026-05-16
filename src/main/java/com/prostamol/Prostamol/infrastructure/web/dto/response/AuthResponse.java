package com.prostamol.Prostamol.infrastructure.web.dto.response;

import java.util.UUID;

public record AuthResponse(
    String token,
    UUID userId,
    String email
) {}
