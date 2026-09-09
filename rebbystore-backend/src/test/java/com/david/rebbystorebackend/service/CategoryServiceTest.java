package com.david.rebbystorebackend.controller;

import com.david.rebbystorebackend.domain.entity.Category;
import com.david.rebbystorebackend.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category category;

    @BeforeEach
    void setUp() {
        category = createCategory(
                "Human Hair",
                "human-hair",
                "Premium human hair wigs"
        );
    }

    @Test
    void shouldCreateCategory() throws Exception {
        String requestBody = """
                {
                  "name": "Bob Wigs",
                  "slug": "bob-wigs",
                  "description": "Classic bob wigs"
                }
                """;

        mockMvc.perform(
                        post("/api/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Bob Wigs"))
                .andExpect(jsonPath("$.slug").value("bob-wigs"))
                .andExpect(jsonPath("$.description").value("Classic bob wigs"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void shouldGetActiveCategories() throws Exception {
        Category activeCategory = category;

        Category inactiveCategory = createCategory(
                "Braided Wigs",
                "braided-wigs",
                "Braided wig styles"
        );

        inactiveCategory.setActive(false);
        inactiveCategory.setUpdatedAt(OffsetDateTime.now());
        categoryRepository.save(inactiveCategory);

        mockMvc.perform(
                        get("/api/categories")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[?(@.id == %d)]"
                        .formatted(activeCategory.getId())).exists())
                .andExpect(jsonPath("$[?(@.id == %d)]"
                        .formatted(inactiveCategory.getId())).doesNotExist());
    }

    @Test
    void shouldGetCategoryById() throws Exception {
        mockMvc.perform(
                        get("/api/categories/{id}", category.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(category.getId()))
                .andExpect(jsonPath("$.name").value("Human Hair"))
                .andExpect(jsonPath("$.slug").value("human-hair"))
                .andExpect(jsonPath("$.description")
                        .value("Premium human hair wigs"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldGetCategoryBySlug() throws Exception {
        mockMvc.perform(
                        get("/api/categories/slug/{slug}", "human-hair")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(category.getId()))
                .andExpect(jsonPath("$.name").value("Human Hair"))
                .andExpect(jsonPath("$.slug").value("human-hair"))
                .andExpect(jsonPath("$.description")
                        .value("Premium human hair wigs"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void shouldUpdateCategory() throws Exception {
        String requestBody = """
                {
                  "name": "Updated Human Hair",
                  "slug": "updated-human-hair",
                  "description": "Updated human hair collection"
                }
                """;

        mockMvc.perform(
                        put("/api/categories/{id}", category.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(category.getId()))
                .andExpect(jsonPath("$.name").value("Updated Human Hair"))
                .andExpect(jsonPath("$.slug").value("updated-human-hair"))
                .andExpect(jsonPath("$.description")
                        .value("Updated human hair collection"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.updatedAt").exists());
    }

    @Test
    void shouldDeactivateCategory() throws Exception {
        mockMvc.perform(
                        delete("/api/categories/{id}", category.getId())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(category.getId()))
                .andExpect(jsonPath("$.name").value("Human Hair"))
                .andExpect(jsonPath("$.slug").value("human-hair"))
                .andExpect(jsonPath("$.active").value(false));

        Category updatedCategory = categoryRepository
                .findById(category.getId())
                .orElseThrow();

        assertEquals(false, updatedCategory.getActive());
    }

    @Test
    void shouldReturnBadRequestForInvalidRequest() throws Exception {
        String requestBody = """
                {
                  "name": "",
                  "slug": "",
                  "description": "Invalid category"
                }
                """;

        mockMvc.perform(
                        post("/api/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message")
                        .value("Request validation failed"))
                .andExpect(jsonPath("$.path").value("/api/categories"))
                .andExpect(jsonPath("$.fieldErrors").exists())
                .andExpect(jsonPath("$.fieldErrors.name").exists())
                .andExpect(jsonPath("$.fieldErrors.slug").exists());
    }

    @Test
    void shouldReturnConflictForDuplicateCategoryName() throws Exception {
        String requestBody = """
                {
                  "name": "Human Hair",
                  "slug": "human-hair-new",
                  "description": "Another category"
                }
                """;

        mockMvc.perform(
                        post("/api/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("Category with name 'Human Hair' already exists"))
                .andExpect(jsonPath("$.path").value("/api/categories"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void shouldReturnConflictForDuplicateCategorySlug() throws Exception {
        String requestBody = """
                {
                  "name": "Lace Front",
                  "slug": "human-hair",
                  "description": "Lace front wigs"
                }
                """;

        mockMvc.perform(
                        post("/api/categories")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("Category with slug 'human-hair' already exists"))
                .andExpect(jsonPath("$.path").value("/api/categories"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void shouldReturnNotFoundForMissingCategory() throws Exception {
        mockMvc.perform(
                        get("/api/categories/{id}", 999999L)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Category with ID '999999' not found"))
                .andExpect(jsonPath("$.path").value("/api/categories/999999"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void shouldReturnNotFoundForMissingCategorySlug() throws Exception {
        mockMvc.perform(
                        get("/api/categories/slug/{slug}", "does-not-exist")
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message")
                        .value("Category with slug 'does-not-exist' not found"))
                .andExpect(jsonPath("$.path")
                        .value("/api/categories/slug/does-not-exist"))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    @Test
    void shouldReturnConflictWhenUpdatingToExistingSlug() throws Exception {
        Category secondCategory = createCategory(
                "Lace Front",
                "lace-front",
                "Lace front wigs"
        );

        String requestBody = """
                {
                  "name": "Updated Lace Front",
                  "slug": "human-hair",
                  "description": "Updated description"
                }
                """;

        mockMvc.perform(
                        put("/api/categories/{id}", secondCategory.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message")
                        .value("Category with slug 'human-hair' already exists"))
                .andExpect(jsonPath("$.path")
                        .value("/api/categories/" + secondCategory.getId()))
                .andExpect(jsonPath("$.fieldErrors").isEmpty());
    }

    private Category createCategory(
            String name,
            String slug,
            String description
    ) {
        Category category = new Category();

        category.setName(name);
        category.setSlug(slug);
        category.setDescription(description);
        category.setActive(true);

        OffsetDateTime now = OffsetDateTime.now();

        category.setCreatedAt(now);
        category.setUpdatedAt(now);

        return categoryRepository.save(category);
    }
}