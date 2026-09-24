package com.david.rebbystorebackend.repository;

import com.david.rebbystorebackend.domain.entity.Product;
import com.david.rebbystorebackend.domain.entity.ProductStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldFindProductBySku() {

        Long categoryId = createCategory(
                "Test Category 1",
                "test-category-1"
        );

        Long productId = createProduct(
                categoryId,
                "Test Wig",
                "TEST-SKU-001",
                "150000.00",
                "active"
        );

        var result = productRepository.findBySku("TEST-SKU-001");

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(productId);
        assertThat(result.get().getName()).isEqualTo("Test Wig");
    }

    @Test
    void shouldCheckWhetherSkuExists() {

        Long categoryId = createCategory(
                "Test Category 2",
                "test-category-2"
        );

        createProduct(
                categoryId,
                "Another Test Wig",
                "TEST-SKU-002",
                "200000.00",
                "active"
        );

        assertThat(productRepository.existsBySku("TEST-SKU-002"))
                .isTrue();

        assertThat(productRepository.existsBySku("DOES-NOT-EXIST"))
                .isFalse();
    }

    @Test
    void shouldFindProductsByStatus() {

        Long categoryId = createCategory(
                "Test Category 3",
                "test-category-3"
        );

        createProduct(
                categoryId,
                "Active Test Wig",
                "TEST-ACTIVE-001",
                "180000.00",
                "active"
        );

        createProduct(
                categoryId,
                "Inactive Test Wig",
                "TEST-INACTIVE-001",
                "190000.00",
                "inactive"
        );

        var results = productRepository.findByStatus(ProductStatus.ACTIVE);

        assertThat(results)
                .extracting(Product::getSku)
                .contains("TEST-ACTIVE-001")
                .doesNotContain("TEST-INACTIVE-001");
    }

    @Test
    void shouldFindProductsByCategoryId() {

        Long categoryId = createCategory(
                "Test Category 4",
                "test-category-4"
        );

        createProduct(
                categoryId,
                "Category Test Wig",
                "TEST-CATEGORY-001",
                "175000.00",
                "active"
        );

        var results = productRepository.findByCategoryId(categoryId);

        assertThat(results)
                .extracting(Product::getSku)
                .contains("TEST-CATEGORY-001");
    }

    private Long createCategory(String name, String slug) {

        return jdbcTemplate.queryForObject(
                """
                INSERT INTO categories (
                    name,
                    slug
                )
                VALUES (?, ?)
                RETURNING id
                """,
                Long.class,
                name,
                slug
        );
    }

    private Long createProduct(
            Long categoryId,
            String name,
            String sku,
            String price,
            String status
    ) {

        return jdbcTemplate.queryForObject(
                """
                INSERT INTO products (
                    category_id,
                    name,
                    sku,
                    price,
                    stock_quantity,
                    low_stock_threshold,
                    status
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """,
                Long.class,
                categoryId,
                name,
                sku,
                new BigDecimal(price),
                10,
                5,
                status
        );
    }
}