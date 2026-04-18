package com.prostamol.Prostamol.application.usecase.category;

import com.prostamol.Prostamol.domain.model.category.Category;
import com.prostamol.Prostamol.domain.port.in.category.GetCategoriesUseCase;
import com.prostamol.Prostamol.domain.port.out.CategoryRepositoryPort;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GetCategoriesService implements GetCategoriesUseCase {

    private final CategoryRepositoryPort categoryRepository;

    public GetCategoriesService(CategoryRepositoryPort categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public List<Category> execute(UUID userId) {
        List<Category> all = new ArrayList<>(categoryRepository.findAllSystemCategories());

        all.addAll(categoryRepository.findAllByUserId(userId));

        return all;
    }
}
