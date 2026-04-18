package com.prostamol.Prostamol.domain.port.out;

import com.prostamol.Prostamol.domain.model.category.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepositoryPort {
    Category save(Category category);
    Optional<Category> findById(UUID id);
    List<Category> findAllByUserId(UUID userId);
    List<Category> findAllSystemCategories();
}
