package com.david.rebbystorebackend.service;

import com.david.rebbystorebackend.domain.entity.Category;
import com.david.rebbystorebackend.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
    }

    @Test
    void shouldCreateCategory() {
        Category category = categoryService.createCategory(
                "Human Hair",
                "human-hair",
                "Premium human hair wigs"
        );

        assertThat(category.getId()).isNotNull();
        assertThat(category.getName()).isEqualTo("Human Hair");
        assertThat(category.getSlug()).isEqualTo("human-hair");
        assertThat(category.getDescription())
                .isEqualTo("Premium human hair wigs");
        assertThat(category.getActive()).isTrue();
        assertThat(category.getCreatedAt()).isNotNull();
        assertThat(category.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldRejectDuplicateCategoryName() {
        categoryService.createCategory(
                "Human Hair",
                "human-hair",
                "Premium human hair wigs"
        );

        assertThatThrownBy(() ->
                categoryService.createCategory(
                        "Human Hair",
                        "human-hair-2",
                        "Another description"
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void shouldRejectDuplicateCategorySlug() {
        categoryService.createCategory(
                "Human Hair",
                "human-hair",
                "Premium human hair wigs"
        );

        assertThatThrownBy(() ->
                categoryService.createCategory(
                        "Lace Front",
                        "human-hair",
                        "Lace front wigs"
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void shouldGetCategoryById() {
        Category created = categoryService.createCategory(
                "Bob Wigs",
                "bob-wigs",
                "Classic bob wigs"
        );

        Category found = categoryService.getCategoryById(created.getId());

        assertThat(found.getName()).isEqualTo("Bob Wigs");
        assertThat(found.getSlug()).isEqualTo("bob-wigs");
    }

    @Test
    void shouldGetCategoryBySlug() {
        categoryService.createCategory(
                "Lace Front",
                "lace-front",
                "Lace front wigs"
        );

        Category found = categoryService.getCategoryBySlug("lace-front");

        assertThat(found.getName()).isEqualTo("Lace Front");
    }

    @Test
    void shouldReturnOnlyActiveCategories() {
        Category active = categoryService.createCategory(
                "Human Hair",
                "human-hair",
                "Human hair wigs"
        );

        Category inactive = categoryService.createCategory(
                "Bob Wigs",
                "bob-wigs",
                "Bob wigs"
        );

        categoryService.deactivateCategory(inactive.getId());

        List<Category> categories = categoryService.getActiveCategories();

        assertThat(categories)
                .extracting(Category::getId)
                .contains(active.getId())
                .doesNotContain(inactive.getId());
    }

    @Test
    void shouldUpdateCategory() {
        Category category = categoryService.createCategory(
                "Human Hair",
                "human-hair",
                "Original description"
        );

        Category updated = categoryService.updateCategory(
                category.getId(),
                "Premium Human Hair",
                "premium-human-hair",
                "Updated description"
        );

        assertThat(updated.getName()).isEqualTo("Premium Human Hair");
        assertThat(updated.getSlug()).isEqualTo("premium-human-hair");
        assertThat(updated.getDescription())
                .isEqualTo("Updated description");
    }

    @Test
    void shouldDeactivateCategory() {
        Category category = categoryService.createCategory(
                "Bob Wigs",
                "bob-wigs",
                "Bob wigs"
        );

        Category deactivated =
                categoryService.deactivateCategory(category.getId());

        assertThat(deactivated.getActive()).isFalse();
    }

    @Test
    void shouldRejectBlankCategoryName() {
        assertThatThrownBy(() ->
                categoryService.createCategory(
                        "",
                        "human-hair",
                        "Description"
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Category name is required");
    }

    @Test
    void shouldRejectBlankCategorySlug() {
        assertThatThrownBy(() ->
                categoryService.createCategory(
                        "Human Hair",
                        " ",
                        "Description"
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Category slug is required");
    }

    @Test
    void shouldRejectUpdatingToExistingSlug() {
        categoryService.createCategory(
                "Human Hair",
                "human-hair",
                "Human hair"
        );

        Category laceFront = categoryService.createCategory(
                "Lace Front",
                "lace-front",
                "Lace front"
        );

        assertThatThrownBy(() ->
                categoryService.updateCategory(
                        laceFront.getId(),
                        "Lace Front Updated",
                        "human-hair",
                        "Updated"
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }
}