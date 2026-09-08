package com.david.rebbystorebackend.service;

import com.david.rebbystorebackend.domain.entity.Category;
import com.david.rebbystorebackend.domain.entity.Product;
import com.david.rebbystorebackend.domain.entity.ProductImage;
import com.david.rebbystorebackend.domain.entity.ProductStatus;
import com.david.rebbystorebackend.repository.CategoryRepository;
import com.david.rebbystorebackend.repository.ProductImageRepository;
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
class ProductImageServiceTest {

    @Autowired
    private ProductImageService productImageService;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        productImageRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    private Category createCategory() {
        Category category = new Category();

        OffsetDateTime now = OffsetDateTime.now();

        category.setName("Human Hair");
        category.setSlug("human-hair");
        category.setDescription("Test category");
        category.setActive(true);
        category.setCreatedAt(now);
        category.setUpdatedAt(now);

        return categoryRepository.save(category);
    }

    private Product createProduct() {
        Category category = createCategory();

        Product product = new Product();

        OffsetDateTime now = OffsetDateTime.now();

        product.setCategory(category);
        product.setName("Test Wig");
        product.setSku("WIG-001");
        product.setDescription("Test product");
        product.setPrice(new BigDecimal("300000"));
        product.setColor("Black");
        product.setTexture("Straight");
        product.setLength("16 inches");
        product.setHairType("Human Hair");
        product.setLowStockThreshold(2);
        product.setStatus(ProductStatus.ACTIVE);
        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        return productRepository.save(product);
    }

    @Test
    void shouldAddImage() {
        Product product = createProduct();

        ProductImage image = productImageService.addImage(
                product.getId(),
                "https://example.com/wig-1.jpg",
                0,
                true
        );

        assertThat(image.getId()).isNotNull();
        assertThat(image.getProduct().getId())
                .isEqualTo(product.getId());
        assertThat(image.getImageUrl())
                .isEqualTo("https://example.com/wig-1.jpg");
        assertThat(image.getSortOrder()).isZero();
        assertThat(image.getPrimary()).isTrue();
        assertThat(image.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldGetProductImagesInSortOrder() {
        Product product = createProduct();

        productImageService.addImage(
                product.getId(),
                "https://example.com/wig-3.jpg",
                2,
                false
        );

        productImageService.addImage(
                product.getId(),
                "https://example.com/wig-1.jpg",
                0,
                true
        );

        productImageService.addImage(
                product.getId(),
                "https://example.com/wig-2.jpg",
                1,
                false
        );

        List<ProductImage> images =
                productImageService.getProductImages(product.getId());

        assertThat(images).hasSize(3);

        assertThat(images.get(0).getSortOrder()).isEqualTo(0);
        assertThat(images.get(1).getSortOrder()).isEqualTo(1);
        assertThat(images.get(2).getSortOrder()).isEqualTo(2);
    }

    @Test
    void shouldAllowOnlyOnePrimaryImage() {
        Product product = createProduct();

        ProductImage first = productImageService.addImage(
                product.getId(),
                "https://example.com/wig-1.jpg",
                0,
                true
        );

        ProductImage second = productImageService.addImage(
                product.getId(),
                "https://example.com/wig-2.jpg",
                1,
                true
        );

        List<ProductImage> images =
                productImageService.getProductImages(product.getId());

        assertThat(images)
                .filteredOn(ProductImage::getPrimary)
                .hasSize(1);

        assertThat(images)
                .filteredOn(image -> image.getId().equals(first.getId()))
                .singleElement()
                .extracting(ProductImage::getPrimary)
                .isEqualTo(false);

        assertThat(second.getPrimary()).isTrue();
    }

    @Test
    void shouldSetPrimaryImage() {
        Product product = createProduct();

        ProductImage first = productImageService.addImage(
                product.getId(),
                "https://example.com/wig-1.jpg",
                0,
                true
        );

        ProductImage second = productImageService.addImage(
                product.getId(),
                "https://example.com/wig-2.jpg",
                1,
                false
        );

        ProductImage updated =
                productImageService.setPrimaryImage(
                        product.getId(),
                        second.getId()
                );

        assertThat(updated.getPrimary()).isTrue();

        ProductImage firstReloaded =
                productImageRepository.findById(first.getId())
                        .orElseThrow();

        assertThat(firstReloaded.getPrimary()).isFalse();
    }

    @Test
    void shouldRejectImageFromDifferentProduct() {
        Product firstProduct = createProduct();

        ProductImage image =
                productImageService.addImage(
                        firstProduct.getId(),
                        "https://example.com/wig-1.jpg",
                        0,
                        true
                );

        Category category = categoryRepository.findBySlug("human-hair")
                .orElseThrow();

        Product secondProduct = new Product();

        OffsetDateTime now = OffsetDateTime.now();

        secondProduct.setCategory(category);
        secondProduct.setName("Second Wig");
        secondProduct.setSku("WIG-002");
        secondProduct.setDescription("Second product");
        secondProduct.setPrice(new BigDecimal("350000"));
        secondProduct.setColor("Brown");
        secondProduct.setTexture("Body Wave");
        secondProduct.setLength("18 inches");
        secondProduct.setHairType("Human Hair");
        secondProduct.setLowStockThreshold(2);
        secondProduct.setStatus(ProductStatus.ACTIVE);
        secondProduct.setCreatedAt(now);
        secondProduct.setUpdatedAt(now);

        Product savedSecondProduct = productRepository.save(secondProduct);

        assertThatThrownBy(() ->
                productImageService.setPrimaryImage(
                        savedSecondProduct.getId(),
                        image.getId()
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not belong");
    }

    @Test
    void shouldDeleteImage() {
        Product product = createProduct();

        ProductImage image = productImageService.addImage(
                product.getId(),
                "https://example.com/wig-1.jpg",
                0,
                false
        );

        productImageService.deleteImage(
                product.getId(),
                image.getId()
        );

        assertThat(productImageRepository.findById(image.getId()))
                .isEmpty();
    }

    @Test
    void shouldPromoteAnotherImageWhenPrimaryIsDeleted() {
        Product product = createProduct();

        ProductImage primary = productImageService.addImage(
                product.getId(),
                "https://example.com/wig-1.jpg",
                0,
                true
        );

        ProductImage second = productImageService.addImage(
                product.getId(),
                "https://example.com/wig-2.jpg",
                1,
                false
        );

        productImageService.deleteImage(
                product.getId(),
                primary.getId()
        );

        ProductImage promoted =
                productImageRepository.findById(second.getId())
                        .orElseThrow();

        assertThat(promoted.getPrimary()).isTrue();
    }

    @Test
    void shouldRejectBlankImageUrl() {
        Product product = createProduct();

        assertThatThrownBy(() ->
                productImageService.addImage(
                        product.getId(),
                        " ",
                        0,
                        false
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Image URL is required");
    }

    @Test
    void shouldRejectNegativeSortOrder() {
        Product product = createProduct();

        assertThatThrownBy(() ->
                productImageService.addImage(
                        product.getId(),
                        "https://example.com/wig.jpg",
                        -1,
                        false
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Sort order cannot be negative");
    }

    @Test
    void shouldRejectNonExistingProduct() {
        assertThatThrownBy(() ->
                productImageService.addImage(
                        999999L,
                        "https://example.com/wig.jpg",
                        0,
                        false
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }
}