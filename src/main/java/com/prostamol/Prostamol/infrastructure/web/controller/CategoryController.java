package com.prostamol.Prostamol.infrastructure.web.controller;

import com.prostamol.Prostamol.domain.model.category.Category;
import com.prostamol.Prostamol.domain.port.in.category.CreateCategoryUseCase;
import com.prostamol.Prostamol.domain.port.in.category.GetCategoriesUseCase;
import com.prostamol.Prostamol.infrastructure.web.dto.request.CreateCategoryRequest;
import com.prostamol.Prostamol.infrastructure.web.dto.response.CategoryResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class CategoryController {

    private final CreateCategoryUseCase createCategory;
    private final GetCategoriesUseCase getCategories;

    public CategoryController(
        CreateCategoryUseCase createCategory,
        GetCategoriesUseCase getCategories
    ) {
        this.createCategory = createCategory;
        this.getCategories = getCategories;
    }

    // ── Authenticated user endpoints ─────────────────────────────────────────

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse create(
        @AuthenticationPrincipal UUID userId,
        @Valid @RequestBody CreateCategoryRequest request
    ) {
        return toResponse(createCategory.execute(
            new CreateCategoryUseCase.Command(userId, request.name(), request.type())
        ));
    }

    @GetMapping("/categories")
    public List<CategoryResponse> list(@AuthenticationPrincipal UUID userId) {
        return getCategories.execute(userId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    // ── Admin endpoints ──────────────────────────────────────────────────────

    @PostMapping("/users/{userId}/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse createByAdmin(
        @PathVariable UUID userId,
        @Valid @RequestBody CreateCategoryRequest request
    ) {
        return toResponse(createCategory.execute(
            new CreateCategoryUseCase.Command(userId, request.name(), request.type())
        ));
    }

    @GetMapping("/users/{userId}/categories")
    public List<CategoryResponse> listByAdmin(@PathVariable UUID userId) {
        return getCategories.execute(userId).stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    private CategoryResponse toResponse(Category c) {
        return new CategoryResponse(
            c.getId(),
            c.getUserId(),
            c.getName(),
            c.getType(),
            c.isSystem()
        );
    }
}
