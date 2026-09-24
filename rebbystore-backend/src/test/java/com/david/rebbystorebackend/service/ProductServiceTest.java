package com.david.rebbystorebackend.service;

import com.david.rebbystorebackend.domain.entity.Category;
import com.david.rebbystorebackend.domain.entity.Product;
import com.david.rebbystorebackend.domain.entity.ProductStatus;
import com.david.rebbystorebackend.exception.ConflictException;
import com.david.rebbystorebackend.exception.ResourceNotFoundException;
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

    // =========================================================
    // SETUP
    // =========================================================

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    // =========================================================
    // TEST DATA HELPERS
    // =========================================================

    private Category createCategory() {
        return createCategory(
                "Human Hair",
                "human-hair",
                true
        );
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

    // =========================================================
    // CREATE PRODUCT
    // =========================================================

    @Test
    void shouldCreateProduct() {

        Category category = createCategory();

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
        assertThat(product.getCategory().getId())
                .isEqualTo(category.getId());
        assertThat(product.getName())
                .isEqualTo("Brazilian Body Wave");
        assertThat(product.getSku())
                .isEqualTo("BW-001");
        assertThat(product.getDescription())
                .isEqualTo("Premium body wave wig");
        assertThat(product.getPrice())
                .isEqualByComparingTo("350000");
        assertThat(product.getColor())
                .isEqualTo("Natural Black");
        assertThat(product.getTexture())
                .isEqualTo("Body Wave");
        assertThat(product.getLength())
                .isEqualTo("18 inches");
        assertThat(product.getHairType())
                .isEqualTo("Human Hair");
        assertThat(product.getStockQuantity())
                .isZero();
        assertThat(product.getLowStockThreshold())
                .isEqualTo(2);
        assertThat(product.getStatus())
                .isEqualTo(ProductStatus.ACTIVE);
    }

    // =========================================================
    // CREATE PRODUCT - DUPLICATE SKU
    // =========================================================

    @Test
    void shouldRejectDuplicateSku() {

        createProduct();

        Category category = categoryRepository.findBySlug("human-hair")
                .orElseThrow();

        assertThatThrownBy(() ->
                productService.createProduct(
                        category.getId(),
                        "Second Wig",
                        "WIG-001",
                        "Second product",
                        new BigDecimal("350000"),
                        "Brown",
                        "Body Wave",
                        "18 inches",
                        "Human Hair",
                        2
                )
        )
                .isInstanceOf(ConflictException.class)
                .hasMessage("Product with SKU 'WIG-001' already exists");
    }

    // =========================================================
    // CREATE PRODUCT - INACTIVE CATEGORY
    // =========================================================

    @Test
    void shouldRejectInactiveCategory() {

        Category category = createCategory(
                "Inactive Hair",
                "inactive-hair",
                false
        );

        assertThatThrownBy(() ->
                productService.createProduct(
                        category.getId(),
                        "Inactive Category Wig",
                        "ICW-001",
                        "Test product",
                        new BigDecimal("300000"),
                        "Black",
                        "Straight",
                        "16 inches",
                        "Human Hair",
                        2
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Cannot create product under an inactive category"
                );
    }

    // =========================================================
    // GET PRODUCT BY ID
    // =========================================================

    @Test
    void shouldGetProductById() {

        Product product = createProduct();

        Product result = productService.getProductById(
                product.getId()
        );

        assertThat(result.getId())
                .isEqualTo(product.getId());
        assertThat(result.getName())
                .isEqualTo("Test Wig");
        assertThat(result.getSku())
                .isEqualTo("WIG-001");
    }

    // =========================================================
    // GET PRODUCT BY ID - NOT FOUND
    // =========================================================

    @Test
    void shouldRejectNonExistingProductById() {

        assertThatThrownBy(() ->
                productService.getProductById(999999L)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product with ID '999999' not found");
    }

    // =========================================================
    // GET PRODUCT BY SKU
    // =========================================================

    @Test
    void shouldGetProductBySku() {

        Product product = createProduct();

        Product result = productService.getProductBySku("WIG-001");

        assertThat(result.getId())
                .isEqualTo(product.getId());
        assertThat(result.getName())
                .isEqualTo("Test Wig");
    }

    // =========================================================
    // GET PRODUCT BY SKU - NOT FOUND
    // =========================================================

    @Test
    void shouldRejectNonExistingProductBySku() {

        assertThatThrownBy(() ->
                productService.getProductBySku("DOES-NOT-EXIST")
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage(
                        "Product with SKU 'DOES-NOT-EXIST' not found"
                );
    }

    // =========================================================
    // GET PRODUCTS BY CATEGORY
    // =========================================================

    @Test
    void shouldGetProductsByCategory() {

        Category category = createCategory();

        Product firstProduct = new Product();
        OffsetDateTime firstNow = OffsetDateTime.now();

        firstProduct.setCategory(category);
        firstProduct.setName("Bob Wig");
        firstProduct.setSku("BOB-001");
        firstProduct.setDescription("Bob product");
        firstProduct.setPrice(new BigDecimal("250000"));
        firstProduct.setColor("Black");
        firstProduct.setTexture("Straight");
        firstProduct.setLength("10 inches");
        firstProduct.setHairType("Human Hair");
        firstProduct.setLowStockThreshold(1);
        firstProduct.setStatus(ProductStatus.ACTIVE);
        firstProduct.setCreatedAt(firstNow);
        firstProduct.setUpdatedAt(firstNow);

        firstProduct = productRepository.save(firstProduct);

        Product secondProduct = new Product();
        OffsetDateTime secondNow = OffsetDateTime.now();

        secondProduct.setCategory(category);
        secondProduct.setName("Lace Front Wig");
        secondProduct.setSku("LF-001");
        secondProduct.setDescription("Lace front product");
        secondProduct.setPrice(new BigDecimal("450000"));
        secondProduct.setColor("Black");
        secondProduct.setTexture("Straight");
        secondProduct.setLength("20 inches");
        secondProduct.setHairType("Human Hair");
        secondProduct.setLowStockThreshold(2);
        secondProduct.setStatus(ProductStatus.ACTIVE);
        secondProduct.setCreatedAt(secondNow);
        secondProduct.setUpdatedAt(secondNow);

        secondProduct = productRepository.save(secondProduct);

        List<Product> products =
                productService.getProductsByCategory(category.getId());

        assertThat(products)
                .hasSize(2);

        assertThat(products)
                .extracting(Product::getId)
                .containsExactlyInAnyOrder(
                        firstProduct.getId(),
                        secondProduct.getId()
                );
    }

    // =========================================================
    // GET PRODUCTS BY CATEGORY - NOT FOUND
    // =========================================================

    @Test
    void shouldRejectNonExistingCategory() {

        assertThatThrownBy(() ->
                productService.getProductsByCategory(999999L)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category with ID '999999' not found");
    }

    // =========================================================
    // GET ACTIVE PRODUCTS
    // =========================================================

    @Test
    void shouldReturnOnlyActiveProducts() {

        Category category = createCategory();

        Product activeProduct = new Product();
        OffsetDateTime activeNow = OffsetDateTime.now();

        activeProduct.setCategory(category);
        activeProduct.setName("Active Wig");
        activeProduct.setSku("ACT-001");
        activeProduct.setDescription("Active product");
        activeProduct.setPrice(new BigDecimal("300000"));
        activeProduct.setColor("Black");
        activeProduct.setTexture("Straight");
        activeProduct.setLength("16 inches");
        activeProduct.setHairType("Human Hair");
        activeProduct.setLowStockThreshold(2);
        activeProduct.setStatus(ProductStatus.ACTIVE);
        activeProduct.setCreatedAt(activeNow);
        activeProduct.setUpdatedAt(activeNow);

        activeProduct = productRepository.save(activeProduct);

        Product inactiveProduct = new Product();
        OffsetDateTime inactiveNow = OffsetDateTime.now();

        inactiveProduct.setCategory(category);
        inactiveProduct.setName("Inactive Wig");
        inactiveProduct.setSku("INA-001");
        inactiveProduct.setDescription("Inactive product");
        inactiveProduct.setPrice(new BigDecimal("320000"));
        inactiveProduct.setColor("Brown");
        inactiveProduct.setTexture("Body Wave");
        inactiveProduct.setLength("18 inches");
        inactiveProduct.setHairType("Human Hair");
        inactiveProduct.setLowStockThreshold(2);
        inactiveProduct.setStatus(ProductStatus.INACTIVE);
        inactiveProduct.setCreatedAt(inactiveNow);
        inactiveProduct.setUpdatedAt(inactiveNow);

        inactiveProduct = productRepository.save(inactiveProduct);

        List<Product> products =
                productService.getActiveProducts();

        assertThat(products)
                .hasSize(1);

        assertThat(products.get(0).getId())
                .isEqualTo(activeProduct.getId());

        assertThat(products.get(0).getStatus())
                .isEqualTo(ProductStatus.ACTIVE);
    }

    // =========================================================
    // UPDATE PRODUCT
    // =========================================================

    @Test
    void shouldUpdateProduct() {

        Product product = createProduct();

        Category category = categoryRepository.findBySlug("human-hair")
                .orElseThrow();

        Product updated = productService.updateProduct(
                product.getId(),
                category.getId(),
                "Updated Wig",
                "WIG-UPDATED",
                "Updated description",
                new BigDecimal("400000"),
                "Brown",
                "Body Wave",
                "20 inches",
                "Human Hair",
                5
        );

        assertThat(updated.getId())
                .isEqualTo(product.getId());
        assertThat(updated.getName())
                .isEqualTo("Updated Wig");
        assertThat(updated.getSku())
                .isEqualTo("WIG-UPDATED");
        assertThat(updated.getDescription())
                .isEqualTo("Updated description");
        assertThat(updated.getPrice())
                .isEqualByComparingTo("400000");
        assertThat(updated.getColor())
                .isEqualTo("Brown");
        assertThat(updated.getTexture())
                .isEqualTo("Body Wave");
        assertThat(updated.getLength())
                .isEqualTo("20 inches");
        assertThat(updated.getHairType())
                .isEqualTo("Human Hair");
        assertThat(updated.getLowStockThreshold())
                .isEqualTo(5);
    }

    // =========================================================
    // UPDATE PRODUCT - DUPLICATE SKU
    // =========================================================

    @Test
    void shouldRejectUpdatingToExistingSku() {

        Category category = createCategory();

        Product firstProduct = new Product();
        OffsetDateTime firstNow = OffsetDateTime.now();

        firstProduct.setCategory(category);
        firstProduct.setName("Product One");
        firstProduct.setSku("SKU-001");
        firstProduct.setDescription("First");
        firstProduct.setPrice(new BigDecimal("300000"));
        firstProduct.setColor("Black");
        firstProduct.setTexture("Straight");
        firstProduct.setLength("16 inches");
        firstProduct.setHairType("Human Hair");
        firstProduct.setLowStockThreshold(2);
        firstProduct.setStatus(ProductStatus.ACTIVE);
        firstProduct.setCreatedAt(firstNow);
        firstProduct.setUpdatedAt(firstNow);

        firstProduct = productRepository.save(firstProduct);

        Product secondProduct = new Product();
        OffsetDateTime secondNow = OffsetDateTime.now();

        secondProduct.setCategory(category);
        secondProduct.setName("Product Two");
        secondProduct.setSku("SKU-002");
        secondProduct.setDescription("Second");
        secondProduct.setPrice(new BigDecimal("350000"));
        secondProduct.setColor("Brown");
        secondProduct.setTexture("Body Wave");
        secondProduct.setLength("18 inches");
        secondProduct.setHairType("Human Hair");
        secondProduct.setLowStockThreshold(2);
        secondProduct.setStatus(ProductStatus.ACTIVE);
        secondProduct.setCreatedAt(secondNow);
        secondProduct.setUpdatedAt(secondNow);

        secondProduct = productRepository.save(secondProduct);

        Long firstProductId = firstProduct.getId();

        assertThatThrownBy(() ->
                productService.updateProduct(
                        firstProductId,
                        category.getId(),
                        "Updated Product",
                        "SKU-002",
                        "Updated description",
                        new BigDecimal("400000"),
                        "Black",
                        "Straight",
                        "20 inches",
                        "Human Hair",
                        3
                )
        )
                .isInstanceOf(ConflictException.class)
                .hasMessage(
                        "Product with SKU 'SKU-002' already exists"
                );
    }

    // =========================================================
    // INVALID PRICE
    // =========================================================

    @Test
    void shouldRejectInvalidPrice() {

        Category category = createCategory();

        assertThatThrownBy(() ->
                productService.createProduct(
                        category.getId(),
                        "Invalid Price Wig",
                        "PRICE-001",
                        "Invalid price product",
                        BigDecimal.ZERO,
                        "Black",
                        "Straight",
                        "16 inches",
                        "Human Hair",
                        2
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Price must be greater than zero");
    }

    // =========================================================
    // DEACTIVATE PRODUCT
    // =========================================================

    @Test
    void shouldDeactivateProduct() {

        Product product = createProduct();

        productService.deactivateProduct(product.getId());

        Product result = productRepository.findById(product.getId())
                .orElseThrow();

        assertThat(result.getStatus())
                .isEqualTo(ProductStatus.INACTIVE);
    }

    // =========================================================
    // DEACTIVATE ALREADY INACTIVE PRODUCT
    // =========================================================

    @Test
    void shouldNotChangeAlreadyInactiveProduct() {

        Product product = createProduct();

        productService.deactivateProduct(product.getId());

        Product result = productRepository.findById(product.getId())
                .orElseThrow();

        assertThat(result.getStatus())
                .isEqualTo(ProductStatus.INACTIVE);

        // Calling deactivate again should be harmless.
        productService.deactivateProduct(product.getId());

        Product afterSecondCall =
                productRepository.findById(product.getId())
                        .orElseThrow();

        assertThat(afterSecondCall.getStatus())
                .isEqualTo(ProductStatus.INACTIVE);
    }

    // =========================================================
    // DEACTIVATE PRODUCT - NOT FOUND
    // =========================================================

    @Test
    void shouldRejectDeactivatingNonExistingProduct() {

        assertThatThrownBy(() ->
                productService.deactivateProduct(999999L)
        )
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product with ID '999999' not found");
    }
}