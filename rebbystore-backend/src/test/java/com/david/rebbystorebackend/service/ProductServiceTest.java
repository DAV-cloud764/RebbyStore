package com.david.rebbystorebackend.service;

import com.david.rebbystorebackend.domain.entity.Category;
import com.david.rebbystorebackend.domain.entity.Product;
import com.david.rebbystorebackend.domain.entity.ProductStatus;
import com.david.rebbystorebackend.repository.CategoryRepository;
import com.david.rebbystorebackend.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    private Category createCategory(
            String name,
            String slug,
            boolean active
    ) {
        Category category = new Category();

        OffsetDateTime now = OffsetDateTime.now();

        category.setName(name);
        category.setSlug(slug);
        category.setDescription("Test category");
        category.setActive(active);
        category.setCreatedAt(now);
        category.setUpdatedAt(now);

        return categoryRepository.save(category);
    }

    @Test
    void shouldCreateProduct() {
        Category category = createCategory(
                "Human Hair",
                "human-hair",
                true
        );

        Product product = productService.createProduct(
                category.getId(),
                "Brazilian Body Wave",
                "BW-001",
                "Premium body wave wig",
                new BigDecimal("350000"),
                "Natural Black",
                "Body Wave",
                "18 inches",
                "Human Hair",
                2
        );

        assertThat(product.getId()).isNotNull();
        assertThat(product.getName()).isEqualTo("Brazilian Body Wave");
        assertThat(product.getSku()).isEqualTo("BW-001");
        assertThat(product.getPrice())
                .isEqualByComparingTo("350000");
        assertThat(product.getCategory().getId())
                .isEqualTo(category.getId());
        assertThat(product.getStockQuantity()).isZero();
        assertThat(product.getLowStockThreshold()).isEqualTo(2);
        assertThat(product.getStatus()).isEqualTo(ProductStatus.ACTIVE);
        assertThat(product.getCreatedAt()).isNotNull();
        assertThat(product.getUpdatedAt()).isNotNull();
    }

    @Test
    void shouldRejectDuplicateSku() {
        Category category = createCategory(
                "Human Hair",
                "human-hair",
                true
        );

        productService.createProduct(
                category.getId(),
                "Brazilian Body Wave",
                "BW-001",
                "First product",
                new BigDecimal("350000"),
                "Black",
                "Body Wave",
                "18 inches",
                "Human Hair",
                2
        );

        assertThatThrownBy(() ->
                productService.createProduct(
                        category.getId(),
                        "Another Wig",
                        "BW-001",
                        "Second product",
                        new BigDecimal("400000"),
                        "Black",
                        "Straight",
                        "20 inches",
                        "Human Hair",
                        2
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void shouldRejectNonExistingCategory() {
        assertThatThrownBy(() ->
                productService.createProduct(
                        999L,
                        "Brazilian Body Wave",
                        "BW-001",
                        "Test product",
                        new BigDecimal("350000"),
                        "Black",
                        "Body Wave",
                        "18 inches",
                        "Human Hair",
                        2
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void shouldRejectInactiveCategory() {
        Category category = createCategory(
                "Human Hair",
                "human-hair",
                false
        );

        assertThatThrownBy(() ->
                productService.createProduct(
                        category.getId(),
                        "Brazilian Body Wave",
                        "BW-001",
                        "Test product",
                        new BigDecimal("350000"),
                        "Black",
                        "Body Wave",
                        "18 inches",
                        "Human Hair",
                        2
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("inactive category");
    }

    @Test
    void shouldGetProductById() {
        Category category = createCategory(
                "Human Hair",
                "human-hair",
                true
        );

        Product created = productService.createProduct(
                category.getId(),
                "Bob Wig",
                "BOB-001",
                "Classic bob",
                new BigDecimal("280000"),
                "Black",
                "Straight",
                "12 inches",
                "Human Hair",
                1
        );

        Product found = productService.getProductById(created.getId());

        assertThat(found.getName()).isEqualTo("Bob Wig");
        assertThat(found.getSku()).isEqualTo("BOB-001");
    }

    @Test
    void shouldGetProductBySku() {
        Category category = createCategory(
                "Lace Front",
                "lace-front",
                true
        );

        productService.createProduct(
                category.getId(),
                "Lace Front Wig",
                "LF-001",
                "Lace front product",
                new BigDecimal("450000"),
                "Black",
                "Straight",
                "20 inches",
                "Human Hair",
                2
        );

        Product found = productService.getProductBySku("LF-001");

        assertThat(found.getName()).isEqualTo("Lace Front Wig");
    }

    @Test
    void shouldReturnOnlyActiveProducts() {
        Category category = createCategory(
                "Human Hair",
                "human-hair",
                true
        );

        Product active = productService.createProduct(
                category.getId(),
                "Active Wig",
                "ACT-001",
                "Active product",
                new BigDecimal("300000"),
                "Black",
                "Straight",
                "16 inches",
                "Human Hair",
                2
        );

        Product inactive = productService.createProduct(
                category.getId(),
                "Inactive Wig",
                "INA-001",
                "Inactive product",
                new BigDecimal("300000"),
                "Black",
                "Straight",
                "16 inches",
                "Human Hair",
                2
        );

        productService.deactivateProduct(inactive.getId());

        List<Product> products = productService.getActiveProducts();

        assertThat(products)
                .extracting(Product::getId)
                .contains(active.getId())
                .doesNotContain(inactive.getId());
    }

    @Test
    void shouldGetProductsByCategory() {
        Category category = createCategory(
                "Bob Wigs",
                "bob-wigs",
                true
        );

        productService.createProduct(
                category.getId(),
                "Bob 1",
                "BOB-001",
                "Bob product",
                new BigDecimal("250000"),
                "Black",
                "Straight",
                "10 inches",
                "Human Hair",
                1
        );

        productService.createProduct(
                category.getId(),
                "Bob 2",
                "BOB-002",
                "Bob product",
                new BigDecimal("270000"),
                "Brown",
                "Straight",
                "12 inches",
                "Human Hair",
                1
        );

        List<Product> products =
                productService.getProductsByCategory(category.getId());

        assertThat(products).hasSize(2);
    }

    @Test
    void shouldUpdateProduct() {
        Category originalCategory = createCategory(
                "Human Hair",
                "human-hair",
                true
        );

        Category newCategory = createCategory(
                "Lace Front",
                "lace-front",
                true
        );

        Product created = productService.createProduct(
                originalCategory.getId(),
                "Original Wig",
                "WIG-001",
                "Original description",
                new BigDecimal("300000"),
                "Black",
                "Straight",
                "16 inches",
                "Human Hair",
                2
        );

        Product updated = productService.updateProduct(
                created.getId(),
                newCategory.getId(),
                "Updated Wig",
                "WIG-002",
                "Updated description",
                new BigDecimal("375000"),
                "Brown",
                "Body Wave",
                "18 inches",
                "Human Hair",
                3
        );

        assertThat(updated.getName()).isEqualTo("Updated Wig");
        assertThat(updated.getSku()).isEqualTo("WIG-002");
        assertThat(updated.getPrice())
                .isEqualByComparingTo("375000");
        assertThat(updated.getCategory().getId())
                .isEqualTo(newCategory.getId());
        assertThat(updated.getDescription())
                .isEqualTo("Updated description");
        assertThat(updated.getLowStockThreshold()).isEqualTo(3);
    }

    @Test
    void shouldRejectUpdatingToExistingSku() {
        Category category = createCategory(
                "Human Hair",
                "human-hair",
                true
        );

        productService.createProduct(
                category.getId(),
                "Product One",
                "SKU-001",
                "First",
                new BigDecimal("300000"),
                "Black",
                "Straight",
                "16 inches",
                "Human Hair",
                2
        );

        Product second = productService.createProduct(
                category.getId(),
                "Product Two",
                "SKU-002",
                "Second",
                new BigDecimal("350000"),
                "Brown",
                "Body Wave",
                "18 inches",
                "Human Hair",
                2
        );

        assertThatThrownBy(() ->
                productService.updateProduct(
                        second.getId(),
                        category.getId(),
                        "Product Two Updated",
                        "SKU-001",
                        "Updated",
                        new BigDecimal("360000"),
                        "Brown",
                        "Body Wave",
                        "18 inches",
                        "Human Hair",
                        2
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void shouldRejectInvalidPrice() {
        Category category = createCategory(
                "Human Hair",
                "human-hair",
                true
        );

        assertThatThrownBy(() ->
                productService.createProduct(
                        category.getId(),
                        "Invalid Price",
                        "BAD-001",
                        "Test",
                        BigDecimal.ZERO,
                        "Black",
                        "Straight",
                        "16 inches",
                        "Human Hair",
                        2
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Product price must be greater than zero"
                );
    }

    @Test
    void shouldDeactivateProduct() {
        Category category = createCategory(
                "Human Hair",
                "human-hair",
                true
        );

        Product product = productService.createProduct(
                category.getId(),
                "Test Wig",
                "WIG-001",
                "Test",
                new BigDecimal("300000"),
                "Black",
                "Straight",
                "16 inches",
                "Human Hair",
                2
        );

        Product deactivated =
                productService.deactivateProduct(product.getId());

        assertThat(deactivated.getStatus())
                .isEqualTo(ProductStatus.INACTIVE);
    }

    @Test
    void shouldNotChangeAlreadyInactiveProduct() {
        Category category = createCategory(
                "Human Hair",
                "human-hair",
                true
        );

        Product product = productService.createProduct(
                category.getId(),
                "Test Wig",
                "WIG-001",
                "Test",
                new BigDecimal("300000"),
                "Black",
                "Straight",
                "16 inches",
                "Human Hair",
                2
        );

        productService.deactivateProduct(product.getId());

        Product result =
                productService.deactivateProduct(product.getId());

        assertThat(result.getStatus())
                .isEqualTo(ProductStatus.INACTIVE);
    }
}