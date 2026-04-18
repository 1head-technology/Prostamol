package com.prostamol.Prostamol.domain.port.in.category;

import com.prostamol.Prostamol.domain.model.category.Category;

import java.util.List;
import java.util.UUID;

public interface GetCategoriesUseCase {
    List<Category> execute(UUID userId);
}
