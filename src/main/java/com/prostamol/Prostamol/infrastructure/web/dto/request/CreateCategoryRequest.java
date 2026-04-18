package com.prostamol.Prostamol.infrastructure.web.dto.request;

import com.prostamol.Prostamol.domain.model.category.CategoryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateCategoryRequest(
    @NotBlank String name,
    @NotNull CategoryType type
) {}
