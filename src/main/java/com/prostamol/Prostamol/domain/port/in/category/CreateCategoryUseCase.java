package com.prostamol.Prostamol.domain.port.in.category;

import com.prostamol.Prostamol.domain.model.category.Category;
import com.prostamol.Prostamol.domain.model.category.CategoryType;

import java.util.UUID;

public interface CreateCategoryUseCase {
    Category execute(Command command);

    record Command(UUID userId, String name, CategoryType type) {}
}
