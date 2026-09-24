package com.david.rebbystorebackend.repository;

import com.david.rebbystorebackend.domain.entity.ProductImage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ProductImageRepositoryTest {

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldFindProductImagesOrderedBySortOrder() {

        Long categoryId = createCategory(
                "Image Test Category",
                "image-test-category"
        );

        Long productId = createProduct(
                categoryId,
                "Image Test Wig",
                "IMAGE-TEST-SKU-001"
        );

        createImage(productId, "image-2.jpg", 2, false);
        createImage(productId, "image-0.jpg", 0, true);
        createImage(productId, "image-1.jpg", 1, false);

        List<ProductImage> results =
                productImageRepository.findByProductIdOrderBySortOrderAsc(productId);

        assertThat(results).hasSize(3);
        assertThat(results.get(0).getImageUrl()).isEqualTo("image-0.jpg");
        assertThat(results.get(1).getImageUrl()).isEqualTo("image-1.jpg");
        assertThat(results.get(2).getImageUrl()).isEqualTo("image-2.jpg");
    }

    @Test
    void shouldFindAllImagesForProduct() {

        Long categoryId = createCategory(
                "Image Collection Category",
                "image-collection-category"
        );

        Long productId = createProduct(
                categoryId,
                "Image Collection Wig",
                "IMAGE-TEST-SKU-002"
        );

        createImage(productId, "front.jpg", 0, true);
        createImage(productId, "side.jpg", 1, false);

        List<ProductImage> results =
                productImageRepository.findByProductId(productId);

        assertThat(results).hasSize(2);
        assertThat(results)
                .extracting(ProductImage::getImageUrl)
                .contains("front.jpg", "side.jpg");
    }

    private Long createCategory(
            String name,
            String slug
    ) {

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
            String sku
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
                new BigDecimal("150000.00"),
                10,
                5,
                "active"
        );
    }

    private Long createImage(
            Long productId,
            String imageUrl,
            int sortOrder,
            boolean primary
    ) {

        return jdbcTemplate.queryForObject(
                """
                INSERT INTO product_images (
                    product_id,
                    image_url,
                    sort_order,
                    is_primary
                )
                VALUES (?, ?, ?, ?)
                RETURNING id
                """,
                Long.class,
                productId,
                imageUrl,
                sortOrder,
                primary
        );
    }
}