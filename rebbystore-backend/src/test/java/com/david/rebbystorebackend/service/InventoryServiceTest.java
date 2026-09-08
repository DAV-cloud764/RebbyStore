package com.david.rebbystorebackend.service;

import com.david.rebbystorebackend.domain.entity.Category;
import com.david.rebbystorebackend.domain.entity.InventoryMovement;
import com.david.rebbystorebackend.domain.entity.Product;
import com.david.rebbystorebackend.domain.entity.ProductStatus;
import com.david.rebbystorebackend.repository.CategoryRepository;
import com.david.rebbystorebackend.repository.InventoryMovementRepository;
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
class InventoryServiceTest {

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    @BeforeEach
    void setUp() {
        inventoryMovementRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    private Product createProduct(
            String sku,
            int lowStockThreshold
    ) {
        Category category = new Category();

        OffsetDateTime now = OffsetDateTime.now();

        category.setName("Human Hair");
        category.setSlug("human-hair");
        category.setDescription("Test category");
        category.setActive(true);
        category.setCreatedAt(now);
        category.setUpdatedAt(now);

        category = categoryRepository.save(category);

        Product product = new Product();

        product.setCategory(category);
        product.setName("Test Wig");
        product.setSku(sku);
        product.setDescription("Test product");
        product.setPrice(new BigDecimal("300000"));
        product.setColor("Black");
        product.setTexture("Straight");
        product.setLength("16 inches");
        product.setHairType("Human Hair");
        product.setLowStockThreshold(lowStockThreshold);
        product.setStatus(ProductStatus.ACTIVE);
        product.setCreatedAt(now);
        product.setUpdatedAt(now);

        return productRepository.save(product);
    }

    @Test
    void shouldStartProductWithZeroStock() {
        Product product = createProduct("WIG-001", 2);

        assertThat(product.getStockQuantity()).isZero();
    }

    @Test
    void shouldIncreaseStock() {
        Product product = createProduct("WIG-001", 2);

        InventoryMovement movement =
                inventoryService.stockIn(
                        product.getId(),
                        10
                );

        assertThat(inventoryService.getCurrentStock(product.getId()))
                .isEqualTo(10);

        assertThat(movement.getType())
                .isEqualTo("in");

        assertThat(movement.getReason())
                .isEqualTo("adjustment");

        assertThat(movement.getQuantity())
                .isEqualTo(10);
    }

    @Test
    void shouldDecreaseStock() {
        Product product = createProduct("WIG-001", 2);

        inventoryService.stockIn(
                product.getId(),
                10
        );

        InventoryMovement movement =
                inventoryService.stockOut(
                        product.getId(),
                        4
                );

        assertThat(inventoryService.getCurrentStock(product.getId()))
                .isEqualTo(6);

        assertThat(movement.getType())
                .isEqualTo("out");

        assertThat(movement.getReason())
                .isEqualTo("adjustment");

        assertThat(movement.getQuantity())
                .isEqualTo(4);
    }

    @Test
    void shouldRejectNegativeStock() {
        Product product = createProduct("WIG-001", 2);

        assertThatThrownBy(() ->
                inventoryService.stockOut(
                        product.getId(),
                        1
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    void shouldAdjustStockUpward() {
        Product product = createProduct("WIG-001", 2);

        InventoryMovement movement =
                inventoryService.adjustStock(
                        product.getId(),
                        5,
                        true
                );

        assertThat(inventoryService.getCurrentStock(product.getId()))
                .isEqualTo(5);

        assertThat(movement.getType())
                .isEqualTo("in");
    }

    @Test
    void shouldAdjustStockDownward() {
        Product product = createProduct("WIG-001", 2);

        inventoryService.stockIn(
                product.getId(),
                8
        );

        InventoryMovement movement =
                inventoryService.adjustStock(
                        product.getId(),
                        3,
                        false
                );

        assertThat(inventoryService.getCurrentStock(product.getId()))
                .isEqualTo(5);

        assertThat(movement.getType())
                .isEqualTo("out");
    }

    @Test
    void shouldIdentifyLowStock() {
        Product product = createProduct("WIG-001", 5);

        inventoryService.stockIn(
                product.getId(),
                5
        );

        assertThat(inventoryService.isLowStock(product.getId()))
                .isTrue();

        inventoryService.stockIn(
                product.getId(),
                1
        );

        assertThat(inventoryService.isLowStock(product.getId()))
                .isFalse();
    }

    @Test
    void shouldReturnProductMovementsNewestFirst() {
        Product product = createProduct("WIG-001", 2);

        inventoryService.stockIn(
                product.getId(),
                10
        );

        inventoryService.stockOut(
                product.getId(),
                2
        );

        List<InventoryMovement> movements =
                inventoryService.getProductMovements(product.getId());

        assertThat(movements)
                .hasSize(2);

        assertThat(movements.get(0).getQuantity())
                .isEqualTo(2);

        assertThat(movements.get(1).getQuantity())
                .isEqualTo(10);
    }

    @Test
    void shouldRejectZeroQuantity() {
        Product product = createProduct("WIG-001", 2);

        assertThatThrownBy(() ->
                inventoryService.stockIn(
                        product.getId(),
                        0
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Inventory quantity must be greater than zero"
                );
    }

    @Test
    void shouldRejectNonExistingProduct() {
        assertThatThrownBy(() ->
                inventoryService.stockIn(
                        999999L,
                        5
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not found");
    }
}