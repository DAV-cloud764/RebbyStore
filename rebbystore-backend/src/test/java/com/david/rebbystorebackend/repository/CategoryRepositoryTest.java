package com.david.rebbystorebackend.repository;

import com.david.rebbystorebackend.domain.entity.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldFindCategoryBySlug() {

        Long categoryId = createCategory(
                "Test Lace Wigs",
                "test-lace-wigs",
                true
        );

        var result = categoryRepository.findBySlug("test-lace-wigs");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(categoryId);
        assertThat(result.get().getName()).isEqualTo("Test Lace Wigs");
    }

    @Test
    void shouldCheckWhetherCategoryNameExists() {

        createCategory(
                "Existing Category",
                "existing-category",
                true
        );

        assertThat(categoryRepository.existsByName("Existing Category"))
                .isTrue();

        assertThat(categoryRepository.existsByName("Non Existing Category"))
                .isFalse();
    }

    @Test
    void shouldCheckWhetherCategorySlugExists() {

        createCategory(
                "Slug Category",
                "slug-category",
                true
        );

        assertThat(categoryRepository.existsBySlug("slug-category"))
                .isTrue();

        assertThat(categoryRepository.existsBySlug("missing-category"))
                .isFalse();
    }

    @Test
    void shouldFindOnlyActiveCategories() {

        createCategory(
                "Active Category",
                "active-category",
                true
        );

        createCategory(
                "Inactive Category",
                "inactive-category",
                false
        );

        List<Category> results = categoryRepository.findByActiveTrue();

        assertThat(results)
                .extracting(Category::getSlug)
                .contains("active-category")
                .doesNotContain("inactive-category");
    }

    private Long createCategory(
            String name,
            String slug,
            boolean active
    ) {

        return jdbcTemplate.queryForObject(
                """
                INSERT INTO categories (
                    name,
                    slug,
                    is_active
                )
                VALUES (?, ?, ?)
                RETURNING id
                """,
                Long.class,
                name,
                slug,
                active
        );
    }
}