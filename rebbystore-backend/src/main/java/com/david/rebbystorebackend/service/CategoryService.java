package com.david.rebbystorebackend.service;

import com.david.rebbystorebackend.domain.entity.Category;
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

    public Category createCategory(String name, String slug, String description) {
        validateRequiredFields(name, slug);

        if (categoryRepository.existsByName(name)) {
            throw new IllegalArgumentException(
                    "Category with name '" + name + "' already exists"
            );
        }

        if (categoryRepository.existsBySlug(slug)) {
            throw new IllegalArgumentException(
                    "Category with slug '" + slug + "' already exists"
            );
        }

        OffsetDateTime now = OffsetDateTime.now();

        Category category = new Category();
        category.setName(name);
        category.setSlug(slug);
        category.setDescription(description);
        category.setActive(true);
        category.setCreatedAt(now);
        category.setUpdatedAt(now);

        return categoryRepository.save(category);
    }

    @Transactional(readOnly = true)
    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Category with id " + id + " not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public Category getCategoryBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Category with slug '" + slug + "' not found"
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

        categoryRepository.findBySlug(slug)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException(
                            "Category with slug '" + slug + "' already exists"
                    );
                });

        if (!category.getName().equals(name)
                && categoryRepository.existsByName(name)) {
            throw new IllegalArgumentException(
                    "Category with name '" + name + "' already exists"
            );
        }

        category.setName(name);
        category.setSlug(slug);
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

    private void validateRequiredFields(String name, String slug) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Category name is required");
        }

        if (slug == null || slug.isBlank()) {
            throw new IllegalArgumentException("Category slug is required");
        }
    }
}