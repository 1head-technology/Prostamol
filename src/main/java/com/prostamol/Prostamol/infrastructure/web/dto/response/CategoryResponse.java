package com.prostamol.Prostamol.infrastructure.web.dto.response;

import com.prostamol.Prostamol.domain.model.category.CategoryType;

import java.util.UUID;

public record CategoryResponse(UUID id, UUID userId, String name, CategoryType type, boolean system) {}
