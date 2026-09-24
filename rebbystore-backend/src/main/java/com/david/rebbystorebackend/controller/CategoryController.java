package com.david.rebbystorebackend.controller;

import com.david.rebbystorebackend.domain.entity.Category;
import com.david.rebbystorebackend.dto.category.CategoryCreateRequest;
import com.david.rebbystorebackend.dto.category.CategoryResponse;
import com.david.rebbystorebackend.dto.category.CategoryUpdateRequest;
import com.david.rebbystorebackend.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getActiveCategories() {
        List<CategoryResponse> categories =
                categoryService.getActiveCategories()
                        .stream()
                        .map(CategoryResponse::from)
                        .toList();

        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(
            @PathVariable Long id
    ) {
        Category category = categoryService.getCategoryById(id);

        return ResponseEntity.ok(
                CategoryResponse.from(category)
        );
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<CategoryResponse> getCategoryBySlug(
            @PathVariable String slug
    ) {
        Category category = categoryService.getCategoryBySlug(slug);

        return ResponseEntity.ok(
                CategoryResponse.from(category)
        );
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CategoryCreateRequest request
    ) {
        Category category = categoryService.createCategory(
                request.name(),
                request.slug(),
                request.description()
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CategoryResponse.from(category));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateRequest request
    ) {
        Category category = categoryService.updateCategory(
                id,
                request.name(),
                request.slug(),
                request.description()
        );

        return ResponseEntity.ok(
                CategoryResponse.from(category)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<CategoryResponse> deactivateCategory(
            @PathVariable Long id
    ) {
        Category category = categoryService.deactivateCategory(id);

        return ResponseEntity.ok(
                CategoryResponse.from(category)
        );
    }
}