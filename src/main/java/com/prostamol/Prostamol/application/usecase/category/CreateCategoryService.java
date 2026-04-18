package com.prostamol.Prostamol.application.usecase.category;

import com.prostamol.Prostamol.domain.model.category.Category;
import com.prostamol.Prostamol.domain.port.in.category.CreateCategoryUseCase;
import com.prostamol.Prostamol.domain.port.out.CategoryRepositoryPort;

import java.util.UUID;

public class CreateCategoryService implements CreateCategoryUseCase {

    private final CategoryRepositoryPort categoryRepository;

    public CreateCategoryService(CategoryRepositoryPort categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Category execute(Command command) {
        return categoryRepository.save(new Category(
            UUID.randomUUID(),
            command.userId(),
            command.name(),
            command.type(),
            false
        ));
    }
}
