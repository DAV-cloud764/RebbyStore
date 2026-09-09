package com.david.rebbystorebackend.service;

import com.david.rebbystorebackend.domain.entity.Category;
import com.david.rebbystorebackend.exception.ConflictException;
import com.david.rebbystorebackend.exception.ResourceNotFoundException;
import com.david.rebbystorebackend.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category createCategory(
            String name,
            String slug,
            String description
    ) {
        validateRequiredFields(name, slug);

        String normalizedName = name.trim();
        String normalizedSlug = slug.trim();

        if (categoryRepository.existsByName(normalizedName)) {
            throw new ConflictException(
                    "Category with name '" + normalizedName + "' already exists"
            );
        }

        if (categoryRepository.existsBySlug(normalizedSlug)) {
            throw new ConflictException(
                    "Category with slug '" + normalizedSlug + "' already exists"
            );
        }

        OffsetDateTime now = OffsetDateTime.now();

        Category category = new Category();

        category.setName(normalizedName);
        category.setSlug(normalizedSlug);
        category.setDescription(description);
        category.setActive(true);
        category.setCreatedAt(now);
        category.setUpdatedAt(now);

        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Category ID is required");
        }

        return categoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category with ID '" + id + "' not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public Category getCategoryBySlug(String slug) {
        if (slug == null || slug.isBlank()) {
            throw new IllegalArgumentException("Category slug is required");
        }

        String normalizedSlug = slug.trim();

        return categoryRepository.findBySlug(normalizedSlug)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Category with slug '" + normalizedSlug + "' not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public List<Category> getActiveCategories() {
        return categoryRepository.findByActiveTrue();
    }

    public Category updateCategory(
            Long id,
            String name,
            String slug,
            String description
    ) {
        validateRequiredFields(name, slug);

        Category category = getCategoryById(id);

        String normalizedName = name.trim();
        String normalizedSlug = slug.trim();

        categoryRepository.findBySlug(normalizedSlug)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new ConflictException(
                            "Category with slug '" + normalizedSlug + "' already exists"
                    );
                });

        if (!category.getName().equals(normalizedName)
                && categoryRepository.existsByName(normalizedName)) {
            throw new ConflictException(
                    "Category with name '" + normalizedName + "' already exists"
            );
        }

        category.setName(normalizedName);
        category.setSlug(normalizedSlug);
        category.setDescription(description);
        category.setUpdatedAt(OffsetDateTime.now());

        return categoryRepository.save(category);
    }

    public Category deactivateCategory(Long id) {
        Category category = getCategoryById(id);

        if (!Boolean.TRUE.equals(category.getActive())) {
            return category;
        }

        category.setActive(false);
        category.setUpdatedAt(OffsetDateTime.now());

        return categoryRepository.save(category);
    }

    private void validateRequiredFields(
            String name,
            String slug
    ) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Category name is required"
            );
        }

        if (slug == null || slug.isBlank()) {
            throw new IllegalArgumentException(
                    "Category slug is required"
            );
        }
    }
}