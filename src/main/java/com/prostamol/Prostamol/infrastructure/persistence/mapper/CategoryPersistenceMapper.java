package com.prostamol.Prostamol.infrastructure.persistence.mapper;

import com.prostamol.Prostamol.domain.model.category.Category;
import com.prostamol.Prostamol.infrastructure.persistence.entity.CategoryJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class CategoryPersistenceMapper {

    public Category toDomain(CategoryJpaEntity entity) {
        return new Category(
            entity.getId(),
            entity.getUserId(),
            entity.getName(),
            entity.getType(),
            entity.isSystem()
        );
    }

    public CategoryJpaEntity toJpaEntity(Category category) {
        return new CategoryJpaEntity(
            category.getId(),
            category.getUserId(),
            category.getName(),
            category.getType(),
            category.isSystem()
        );
    }
}
